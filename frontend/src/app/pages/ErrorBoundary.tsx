import { useRouteError } from "react-router";

export default function ErrorBoundary() {
  const error = useRouteError() as any;
  
  // Se for um erro de importação dinâmica do Vite (ex: chunk antigo), recarregar a página
  if (error && (error.message?.includes('dynamically imported module') || error.message?.includes('useLocation() may be used only in the context of a <Router> component.'))) {
    window.location.reload();
  }

  return (
    <div className="flex h-screen w-full flex-col items-center justify-center bg-gray-50 p-6 text-center">
      <div className="mb-4 text-6xl">🚨</div>
      <h1 className="mb-2 text-2xl font-bold text-gray-800">Oops! Algo deu errado.</h1>
      <p className="mb-6 text-gray-600">
        A página que você está tentando acessar não existe ou ocorreu um erro inesperado.
      </p>
      {error && (
        <pre className="mb-6 max-w-2xl overflow-auto rounded bg-white p-4 text-left text-sm text-red-500 shadow">
          {error.message || String(error)}
        </pre>
      )}
      <button 
        onClick={() => window.location.href = "/"}
        className="rounded bg-blue-500 px-6 py-2 font-bold text-white hover:bg-blue-600 transition-colors"
      >
        Voltar para a tela inicial
      </button>
    </div>
  );
}
