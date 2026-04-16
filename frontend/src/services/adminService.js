import apiClient from "./apiClient";

export const adminService = {
  async getDashboard() {
    const response = await apiClient.get("/admin/dashboard");
    return response.data;
  },
  async getBatches() {
    const response = await apiClient.get("/admin/batches");
    return response.data;
  },
  async getTrainers() {
    const response = await apiClient.get("/admin/trainers");
    return response.data;
  },
  async createTrainer(payload) {
    const response = await apiClient.post("/admin/trainers", payload);
    return response.data;
  }
};
