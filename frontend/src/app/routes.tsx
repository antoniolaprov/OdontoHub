import { createBrowserRouter } from "react-router";
import Home from "./pages/Home";
import Login from "./pages/Login";
import RecepcionistaLayout from "./pages/recepcionista/RecepcionistaLayout";
import RecepcionistaAgenda from "./pages/recepcionista/Agenda";
import RecepcionistaAcordos from "./pages/recepcionista/Acordos";
import RecepcionistaPagamentos from "./pages/recepcionista/Pagamentos";
import RecepcionistaRecall from "./pages/recepcionista/Recall";
import RecepcionistaPacientes from "./pages/recepcionista/Pacientes";
import AuxiliarLayout from "./pages/auxiliar/AuxiliarLayout";
import AuxiliarEstoque from "./pages/auxiliar/Estoque";
import AuxiliarEsterilizacao from "./pages/auxiliar/Esterilizacao";
import AuxiliarInstrumentos from "./pages/auxiliar/Instrumentos";
import DentistaLayout from "./pages/dentista/DentistaLayout";
import DentistaAgenda from "./pages/dentista/Agenda";
import DentistaProntuarios from "./pages/dentista/Prontuarios";
import DentistaFinanceiro from "./pages/dentista/Financeiro";
import DentistaDashboard from "./pages/dentista/Dashboard";
import DentistaEquipe from "./pages/dentista/Equipe";
import ErrorBoundary from "./pages/ErrorBoundary";
import DentistaMedicamentos from "./pages/dentista/Medicamentos";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Login,
    ErrorBoundary: ErrorBoundary,
  },
  {
    path: "/login",
    Component: Login,
    ErrorBoundary: ErrorBoundary,
  },
  {
    path: "/portal",
    Component: Home,
    ErrorBoundary: ErrorBoundary,
  },
  {
    path: "/recepcionista",
    Component: RecepcionistaLayout,
    ErrorBoundary: ErrorBoundary,
    children: [
      { index: true, Component: RecepcionistaAgenda },
      { path: "pacientes", Component: RecepcionistaPacientes },
      { path: "recall",    Component: RecepcionistaRecall },
      { path: "acordos",    Component: RecepcionistaAcordos },
      { path: "pagamentos", Component: RecepcionistaPagamentos },
    ],
  },
  {
    path: "/auxiliar",
    Component: AuxiliarLayout,
    ErrorBoundary: ErrorBoundary,
    children: [
      { index: true, Component: AuxiliarEstoque },
      { path: "esterilizacao",  Component: AuxiliarEsterilizacao },
      { path: "instrumentos",   Component: AuxiliarInstrumentos },
    ],
  },
  {
    path: "/dentista",
    Component: DentistaLayout,
    ErrorBoundary: ErrorBoundary,
    children: [
      { index: true, Component: DentistaAgenda },
      { path: "pacientes", Component: DentistaProntuarios },
      { path: "financeiro", Component: DentistaFinanceiro },
      { path: "dashboards", Component: DentistaDashboard },
      { path: "equipe", Component: DentistaEquipe },
      { path: "medicamentos", Component: DentistaMedicamentos },
    ],
  },
]);
