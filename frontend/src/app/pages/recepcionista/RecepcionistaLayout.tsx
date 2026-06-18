import { Outlet } from "react-router";
import Sidebar from "../../components/Sidebar";

export default function RecepcionistaLayout() {
  const menuItems = [
    { path: "",          label: "Agenda",        icon: "📅" },
    { path: "pacientes", label: "Pacientes",     icon: "👤" },
    { path: "recall",    label: "Fila de Recall", icon: "📞" },
    { path: "acordos",    label: "Acordos",       icon: "💰" },
    { path: "pagamentos", label: "Pagamentos",    icon: "💳" },
  ];

  return (
    <div className="flex h-screen bg-white">
      <Sidebar basePath="/recepcionista" items={menuItems} />
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
