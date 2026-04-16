const TOKEN_KEY = "token";
const ROLE_KEY = "role";
const NAME_KEY = "name";
const EMAIL_KEY = "email";

export const storage = {
  setAuth(data) {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(ROLE_KEY, data.role);
    localStorage.setItem(NAME_KEY, data.name);
    localStorage.setItem(EMAIL_KEY, data.email);
  },
  getToken() {
    return localStorage.getItem(TOKEN_KEY);
  },
  getRole() {
    return localStorage.getItem(ROLE_KEY);
  },
  getName() {
    return localStorage.getItem(NAME_KEY);
  },
  getEmail() {
    return localStorage.getItem(EMAIL_KEY);
  },
  clearAuth() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROLE_KEY);
    localStorage.removeItem(NAME_KEY);
    localStorage.removeItem(EMAIL_KEY);
  }
};
