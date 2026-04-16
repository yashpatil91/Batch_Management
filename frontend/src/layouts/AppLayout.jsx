import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";

const AppLayout = ({ children }) => {
  return (
    <div className="min-h-screen bg-slate-950 md:flex">
      <Sidebar />
      <div className="flex-1">
        <Navbar />
        <main className="p-5 md:p-7">{children}</main>
      </div>
    </div>
  );
};

export default AppLayout;
