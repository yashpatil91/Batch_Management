import apiClient from "./apiClient";

export const authService = {
  async login(payload) {
    const response = await apiClient.post("/auth/login", payload);
    return response.data;
  }
};
