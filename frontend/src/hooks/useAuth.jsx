import { createContext, useContext, useMemo, useState } from "react";
import { storage } from "../utils/storage";

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [auth, setAuth] = useState({
    token: storage.getToken(),
    role: storage.getRole(),
    name: storage.getName(),
    email: storage.getEmail()
  });

  const value = useMemo(
    () => ({
      ...auth,
      isAuthenticated: Boolean(auth.token),
      login: (loginResponse) => {
        storage.setAuth(loginResponse);
        setAuth({
          token: loginResponse.token,
          role: loginResponse.role,
          name: loginResponse.name,
          email: loginResponse.email
        });
      },
      logout: () => {
        storage.clearAuth();
        setAuth({ token: null, role: null, name: null, email: null });
      }
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
};
