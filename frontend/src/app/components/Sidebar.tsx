import { Link, useLocation } from "react-router";

interface SidebarProps {
  basePath: string;
  items: { path: string; label: string; icon: string }[];
}

export default function Sidebar({
  basePath,
  items,
}: SidebarProps) {
  const location = useLocation();

  return (
    <aside className="w-64 border-r-2 border-gray-300 bg-gray-50 p-6">
      <div className="mb-4">
        <Link
          to="/"
          className="h-12 border-2 border-gray-400 flex items-center justify-center bg-white hover:border-blue-500 hover:bg-blue-50 transition-colors"
        >
          <span className="font-bold text-gray-700">
            OdontoHub
          </span>
        </Link>
      </div>

      <Link
        to="/portal"
        className="mb-6 block p-2 border-2 border-gray-300 bg-white text-center text-sm font-bold text-gray-600 hover:border-blue-400 hover:bg-blue-50 transition-colors"
      >
        ← Trocar perfil
      </Link>

      <nav className="space-y-2">
        {items.map((item) => {
          const fullPath =
            item.path === ""
              ? basePath
              : `${basePath}/${item.path}`;

          const isActive = location.pathname === fullPath;

          return (
            <Link
              key={item.path}
              to={fullPath}
              className={`block p-3 border-2 ${
                isActive
                  ? "border-blue-500 bg-blue-50"
                  : "border-gray-300 bg-white"
              } hover:border-blue-400`}
            >
              <div className="flex items-center gap-3">
                <span className="text-lg">{item.icon}</span>
                <span className="text-gray-700">
                  {item.label}
                </span>
              </div>
            </Link>
          );
        })}
      </nav>
    </aside>
  );
}