import { NavLink } from "react-router-dom";

const Sidebar = () => {
  return (
    <aside className="w-full border-b border-slate-800 bg-slate-950 p-4 text-white md:min-h-screen md:w-64 md:border-b-0 md:border-r">
      <p className="mb-1 text-lg font-semibold">Batch Management</p>
      <p className="mb-5 text-xs text-slate-400">Admin Workspace</p>
      <nav className="flex flex-row gap-2 md:flex-col">
        <NavLink
          to="/admin/dashboard"
          className={({ isActive }) =>
            `rounded-md px-3 py-2 text-sm ${isActive ? "bg-blue-600 text-white" : "text-slate-300 hover:bg-slate-800"}`
          }
        >
          Dashboard
        </NavLink>
        <NavLink
          to="/admin/trainers"
          className={({ isActive }) =>
            `rounded-md px-3 py-2 text-sm ${isActive ? "bg-blue-600 text-white" : "text-slate-200 hover:bg-slate-800"}`
          }
        >
          Trainers
        </NavLink>
      </nav>
    </aside>
  );
};

export default Sidebar;
