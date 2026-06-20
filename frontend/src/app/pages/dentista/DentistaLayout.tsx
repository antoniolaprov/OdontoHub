import { Outlet } from "react-router";
import Sidebar from "../../components/Sidebar";

export default function DentistaLayout() {
  const menuItems = [
    { path: "", label: "Agenda", icon: "📅" },
    { path: "pacientes", label: "Pacientes", icon: "👤" },
    { path: "financeiro", label: "Financeiro", icon: "💰" },
    { path: "dashboards", label: "Dashboards", icon: "📊" },
    { path: "equipe", label: "Equipe", icon: "👥" },
    { path: "medicamentos", label: "Medicamentos", icon: "💊" },
  ];

  return (
    <div className="flex h-screen bg-white">
      <Sidebar basePath="/dentista" items={menuItems} />
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
