import { Outlet } from "react-router";
import Sidebar from "../../components/Sidebar";

export default function AuxiliarLayout() {
  const menuItems = [
    { path: "",             label: "Estoque",       icon: "📦" },
    { path: "esterilizacao", label: "Esterilização", icon: "🧪" },
    { path: "instrumentos",  label: "Instrumentos",  icon: "🔧" },
  ];

  return (
    <div className="flex h-screen bg-white">
      <Sidebar basePath="/auxiliar" items={menuItems} />
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
