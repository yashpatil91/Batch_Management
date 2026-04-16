import { useEffect, useState } from "react";
import AppLayout from "../layouts/AppLayout";
import Card from "../components/Card";
import Input from "../components/Input";
import Button from "../components/Button";
import { adminService } from "../services/adminService";
import { validateTrainerForm } from "../utils/validation";

const initialForm = { name: "", email: "", password: "" };

const TrainerManagementPage = () => {
  const [trainers, setTrainers] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [errors, setErrors] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [error, setError] = useState("");

  const loadTrainers = async () => {
    try {
      setIsLoading(true);
      const data = await adminService.getTrainers();
      setTrainers(data);
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to fetch trainers");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    loadTrainers();
  }, []);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateTrainer = async (event) => {
    event.preventDefault();
    const validationErrors = validateTrainerForm(form);
    setErrors(validationErrors);
    if (Object.keys(validationErrors).length > 0) return;

    try {
      setIsCreating(true);
      setError("");
      await adminService.createTrainer(form);
      setForm(initialForm);
      setErrors({});
      await loadTrainers();
    } catch (err) {
      setError(err?.response?.data?.message || "Failed to create trainer");
    } finally {
      setIsCreating(false);
    }
  };

  return (
    <AppLayout>
      <div className="grid gap-5 lg:grid-cols-3">
        <Card title="Create Trainer" className="lg:col-span-1">
          <form onSubmit={handleCreateTrainer} className="space-y-3">
            <Input label="Name" name="name" value={form.name} onChange={handleChange} error={errors.name} />
            <Input label="Email" name="email" value={form.email} onChange={handleChange} error={errors.email} />
            <Input
              label="Password"
              name="password"
              type="password"
              value={form.password}
              onChange={handleChange}
              error={errors.password}
            />
            <Button type="submit" loading={isCreating} className="w-full">
              Add Trainer
            </Button>
          </form>
        </Card>

        <Card title="Trainer List" className="lg:col-span-2">
          {error && <p className="mb-3 rounded-md bg-red-500/10 p-2 text-sm text-red-300">{error}</p>}
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-slate-900 text-slate-400">
                <tr>
                  <th className="px-3 py-2">ID</th>
                  <th className="px-3 py-2">Name</th>
                  <th className="px-3 py-2">Email</th>
                  <th className="px-3 py-2">Role</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td className="px-3 py-3 text-slate-400" colSpan={4}>
                      Loading trainers...
                    </td>
                  </tr>
                ) : trainers.length === 0 ? (
                  <tr>
                    <td className="px-3 py-3 text-slate-400" colSpan={4}>
                      No trainers found.
                    </td>
                  </tr>
                ) : (
                  trainers.map((trainer) => (
                    <tr key={trainer.id} className="border-t border-slate-800 text-slate-200">
                      <td className="px-3 py-2">{trainer.id}</td>
                      <td className="px-3 py-2">{trainer.name}</td>
                      <td className="px-3 py-2">{trainer.email}</td>
                      <td className="px-3 py-2">{trainer.role}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </AppLayout>
  );
};

export default TrainerManagementPage;
