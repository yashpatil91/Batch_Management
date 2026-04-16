import apiClient from "./apiClient";

export const trainerService = {
  async getAssignedBatches() {
    const response = await apiClient.get("/trainer/batches");
    return response.data;
  }
};
