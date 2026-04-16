import { useEffect, useState } from "react";
import Card from "../components/Card";
import Button from "../components/Button";
import { useAuth } from "../hooks/useAuth";
import { trainerService } from "../services/trainerService";

const TrainerDashboardPage = () => {
  const { name, logout } = useAuth();
  const [batches, setBatches] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const loadBatches = async () => {
      try {
        setIsLoading(true);
        const data = await trainerService.getAssignedBatches();
        setBatches(data);
      } catch (err) {
        setError(err?.response?.data?.message || "Failed to fetch assigned batches");
      } finally {
        setIsLoading(false);
      }
    };
    loadBatches();
  }, []);

  return (
    <div className="min-h-screen bg-slate-100 p-5 md:p-8">
      <div className="mb-5 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Trainer Dashboard</h1>
          <p className="text-sm text-slate-500">Welcome, {name}</p>
        </div>
        <Button variant="secondary" onClick={logout}>
          Logout
        </Button>
      </div>

      <Card title="Assigned Batches">
        {error && <p className="mb-3 rounded-md bg-red-50 p-2 text-sm text-red-700">{error}</p>}
        <div className="space-y-3">
          {isLoading ? (
            <p className="text-sm text-slate-500">Loading batches...</p>
          ) : batches.length === 0 ? (
            <p className="text-sm text-slate-500">No assigned batches found.</p>
          ) : (
            batches.map((batch) => (
              <div key={batch.id} className="rounded-md border border-slate-200 p-3">
                <p className="font-semibold text-slate-800">{batch.domainName}</p>
                <p className="text-sm text-slate-500">
                  {batch.startDate} - {batch.endDate} | {batch.status}
                </p>
                <div className="mt-2 h-2 w-full rounded bg-slate-200">
                  <div className="h-2 rounded bg-green-500" style={{ width: `${batch.progress}%` }} />
                </div>
              </div>
            ))
          )}
        </div>
      </Card>
    </div>
  );
};

export default TrainerDashboardPage;
