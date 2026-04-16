import Button from "./Button";
import { useAuth } from "../hooks/useAuth";

const Navbar = () => {
  const { name, role, logout } = useAuth();

  return (
    <header className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 bg-slate-950/60 px-5 py-4 backdrop-blur">
      <div>
        <p className="text-lg font-semibold text-slate-100">Welcome, {name || "User"}</p>
        <p className="text-sm text-slate-400">{role}</p>
      </div>
      <Button variant="secondary" onClick={logout}>
        Logout
      </Button>
    </header>
  );
};

export default Navbar;
