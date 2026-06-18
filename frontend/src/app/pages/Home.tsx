import { Link } from "react-router";

export default function Home() {
  return (
    <div className="size-full bg-gray-100 flex items-center justify-center p-8">
      <div className="max-w-4xl w-full">
        <div className="text-center mb-12">
          <div className="h-20 border-4 border-gray-400 mb-6 flex items-center justify-center bg-white">
            <span className="text-2xl font-bold text-gray-700">OdontoHub</span>
          </div>
          <p className="text-gray-600">Sistema de Gestão para Clínicas Odontológicas</p>
        </div>

        <div className="grid grid-cols-3 gap-8">
          <Link
            to="/recepcionista"
            className="border-4 border-gray-400 bg-white p-8 hover:border-blue-500 hover:bg-blue-50"
          >
            <div className="text-center">
              <div className="h-24 border-2 border-gray-300 mb-4 flex items-center justify-center bg-gray-50">
                <span className="text-4xl">📅</span>
              </div>
              <h3 className="font-bold text-gray-700 mb-2">Recepcionista</h3>
              <p className="text-sm text-gray-500">Agenda e Atendimento</p>
            </div>
          </Link>

          <Link
            to="/auxiliar"
            className="border-4 border-gray-400 bg-white p-8 hover:border-blue-500 hover:bg-blue-50"
          >
            <div className="text-center">
              <div className="h-24 border-2 border-gray-300 mb-4 flex items-center justify-center bg-gray-50">
                <span className="text-4xl">📦</span>
              </div>
              <h3 className="font-bold text-gray-700 mb-2">Auxiliar</h3>
              <p className="text-sm text-gray-500">Estoque e Biossegurança</p>
            </div>
          </Link>

          <Link
            to="/dentista"
            className="border-4 border-gray-400 bg-white p-8 hover:border-blue-500 hover:bg-blue-50"
          >
            <div className="text-center">
              <div className="h-24 border-2 border-gray-300 mb-4 flex items-center justify-center bg-gray-50">
                <span className="text-4xl">🦷</span>
              </div>
              <h3 className="font-bold text-gray-700 mb-2">Cirurgião-Dentista</h3>
              <p className="text-sm text-gray-500">Clínico e Gerencial</p>
            </div>
          </Link>
        </div>
      </div>
    </div>
  );
}
