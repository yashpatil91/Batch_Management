import { useEffect, useMemo, useState } from "react";
import { adminService } from "../services/adminService";

const todayDateString = () => new Date().toISOString().split("T")[0];

export const useDashboardAnalytics = () => {
  const [dashboard, setDashboard] = useState({
    totalTrainers: 0,
    totalBatches: 0,
    ongoingBatches: 0
  });
  const [trainers, setTrainers] = useState([]);
  const [batches, setBatches] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadData = async () => {
      try {
        setIsLoading(true);
        setError("");
        const [dashboardResult, trainersResult, batchesResult] = await Promise.allSettled([
          adminService.getDashboard(),
          adminService.getTrainers(),
          adminService.getBatches()
        ]);

        if (dashboardResult.status === "fulfilled") {
          setDashboard(dashboardResult.value);
        } else {
          throw dashboardResult.reason;
        }

        if (trainersResult.status === "fulfilled") {
          setTrainers(trainersResult.value);
        } else {
          setTrainers([]);
        }

        if (batchesResult.status === "fulfilled") {
          setBatches(batchesResult.value);
        } else {
          setBatches([]);
          setError("Batch analytics unavailable because /api/admin/batches is not accessible.");
        }
      } catch (err) {
        setError(err?.response?.data?.message || "Failed to load dashboard data");
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, []);

  const analytics = useMemo(() => {
    const today = todayDateString();
    const completedBatches = batches.filter(
      (batch) => batch.status === "COMPLETED" || Number(batch.progress) === 100
    );
    const ongoingBatches = batches.filter(
      (batch) => batch.status === "ONGOING" && batch.startDate <= today && Number(batch.progress) < 100
    );
    const upcomingBatches = batches.filter((batch) => batch.startDate > today);

    const trainerWorkloadMap = new Map();
    batches.forEach((batch) => {
      const trainerName = batch.trainerName || "Unassigned";
      trainerWorkloadMap.set(trainerName, (trainerWorkloadMap.get(trainerName) || 0) + 1);
    });
    const trainerWorkload = Array.from(trainerWorkloadMap.entries())
      .map(([trainerName, batchCount]) => ({ trainerName, batchCount }))
      .sort((a, b) => b.batchCount - a.batchCount);

    const recentTrainers = [...trainers].sort((a, b) => b.id - a.id).slice(0, 5);
    const recentBatches = [...batches].sort((a, b) => b.id - a.id).slice(0, 5);

    const totalStatusCount = ongoingBatches.length + completedBatches.length + upcomingBatches.length;

    return {
      completedCount: completedBatches.length,
      ongoingCount: ongoingBatches.length,
      upcomingCount: upcomingBatches.length,
      totalStatusCount,
      trainerWorkload,
      recentTrainers,
      recentBatches
    };
  }, [batches, trainers]);

  return {
    dashboard,
    isLoading,
    error,
    analytics
  };
};
