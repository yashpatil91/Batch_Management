import Card from "../components/Card";
import AppLayout from "../layouts/AppLayout";
import { useDashboardAnalytics } from "../hooks/useDashboardAnalytics";

const metricCardClass = "bg-gradient-to-br from-slate-900 to-slate-800";

const statusBars = (analytics) => [
  { key: "ongoing", label: "Ongoing", count: analytics.ongoingCount, color: "bg-blue-500" },
  { key: "completed", label: "Completed", count: analytics.completedCount, color: "bg-emerald-500" },
  { key: "upcoming", label: "Upcoming", count: analytics.upcomingCount, color: "bg-amber-500" }
];

const AdminDashboardPage = () => {
  const { dashboard, isLoading, error, analytics } = useDashboardAnalytics();
  const completedBatches = Math.max(dashboard.totalBatches - dashboard.ongoingBatches, 0);

  return (
    <AppLayout>
      {error && <p className="mb-4 rounded-md bg-red-500/10 p-3 text-sm text-red-300">{error}</p>}

      <div className="mb-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Card title="Total Trainers" className={metricCardClass}>
          <p className="text-3xl font-bold text-white">{isLoading ? "..." : dashboard.totalTrainers}</p>
        </Card>
        <Card title="Total Batches" className={metricCardClass}>
          <p className="text-3xl font-bold text-white">{isLoading ? "..." : dashboard.totalBatches}</p>
        </Card>
        <Card title="Ongoing Batches" className={metricCardClass}>
          <p className="text-3xl font-bold text-white">{isLoading ? "..." : dashboard.ongoingBatches}</p>
        </Card>
        <Card title="Completed Batches" className={metricCardClass}>
          <p className="text-3xl font-bold text-white">{isLoading ? "..." : completedBatches}</p>
        </Card>
      </div>

      <div className="grid gap-5 xl:grid-cols-3">
        <Card title="Batch Status Overview" className="xl:col-span-1">
          <div className="space-y-4">
            {statusBars(analytics).map((item) => {
              const percent = analytics.totalStatusCount > 0 ? (item.count / analytics.totalStatusCount) * 100 : 0;
              return (
                <div key={item.key}>
                  <div className="mb-1 flex items-center justify-between text-sm">
                    <span className="text-slate-300">{item.label}</span>
                    <span className="font-semibold text-white">{item.count}</span>
                  </div>
                  <div className="h-2 rounded bg-slate-800">
                    <div className={`h-2 rounded ${item.color}`} style={{ width: `${percent}%` }} />
                  </div>
                </div>
              );
            })}
          </div>
        </Card>

        <Card title="Trainer Workload" className="xl:col-span-2">
          <div className="space-y-3">
            {isLoading ? (
              <p className="text-sm text-slate-400">Loading workload...</p>
            ) : analytics.trainerWorkload.length === 0 ? (
              <p className="text-sm text-slate-400">No batch assignments found.</p>
            ) : (
              analytics.trainerWorkload.map((item) => {
                const maxCount = analytics.trainerWorkload[0]?.batchCount || 1;
                const percent = (item.batchCount / maxCount) * 100;
                return (
                  <div key={item.trainerName}>
                    <div className="mb-1 flex items-center justify-between text-sm">
                      <span className="text-slate-300">{item.trainerName}</span>
                      <span className="font-semibold text-white">{item.batchCount} batches</span>
                    </div>
                    <div className="h-2 rounded bg-slate-800">
                      <div className="h-2 rounded bg-violet-500" style={{ width: `${percent}%` }} />
                    </div>
                  </div>
                );
              })
            )}
          </div>
        </Card>
      </div>

      <div className="mt-5 grid gap-5 xl:grid-cols-2">
        <Card title="Recent Activity - Trainers">
          {isLoading ? (
            <p className="text-sm text-slate-400">Loading trainers...</p>
          ) : analytics.recentTrainers.length === 0 ? (
            <p className="text-sm text-slate-400">No trainers yet.</p>
          ) : (
            <div className="space-y-2">
              {analytics.recentTrainers.map((trainer) => (
                <div key={trainer.id} className="rounded-lg border border-slate-800 bg-slate-950/50 p-3">
                  <p className="font-medium text-slate-100">{trainer.name}</p>
                  <p className="text-xs text-slate-400">{trainer.email}</p>
                </div>
              ))}
            </div>
          )}
        </Card>

        <Card title="Recent Activity - Batches">
          {isLoading ? (
            <p className="text-sm text-slate-400">Loading batches...</p>
          ) : analytics.recentBatches.length === 0 ? (
            <p className="text-sm text-slate-400">No batches yet.</p>
          ) : (
            <div className="space-y-2">
              {analytics.recentBatches.map((batch) => (
                <div key={batch.id} className="rounded-lg border border-slate-800 bg-slate-950/50 p-3">
                  <p className="font-medium text-slate-100">{batch.domainName}</p>
                  <p className="text-xs text-slate-400">
                    {batch.status} • Trainer: {batch.trainerName || "Unassigned"}
                  </p>
                </div>
              ))}
            </div>
          )}
        </Card>
      </div>
    </AppLayout>
  );
};

export default AdminDashboardPage;
