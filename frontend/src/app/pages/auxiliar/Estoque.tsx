export default function AuxiliarEstoque() {
  const materiais = [
    { nome: "Luvas Descartáveis (cx)", quantidade: 150, custo: "R$ 45,00", baixo: false },
    { nome: "Anestésico Lidocaína 2%", quantidade: 8, custo: "R$ 12,50", baixo: true },
    { nome: "Algodão Hidrófilo (pacote)", quantidade: 35, custo: "R$ 8,00", baixo: false },
    { nome: "Resina Composta A2", quantidade: 3, custo: "R$ 85,00", baixo: true },
    { nome: "Fio de Sutura 4-0", quantidade: 12, custo: "R$ 15,00", baixo: false },
    { nome: "Agulha Gengival Curta", quantidade: 5, custo: "R$ 3,50", baixo: true },
    { nome: "Máscara Tripla Camada (cx)", quantidade: 200, custo: "R$ 28,00", baixo: false },
  ];

  return (
    <div className="p-6">
      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-xl font-bold text-gray-700">Controle de Estoque</h1>
        <button className="px-4 py-2 border-2 border-blue-500 bg-blue-500 text-white font-bold">
          + Registrar Reposição
        </button>
      </div>

      <div className="border-2 border-gray-400 bg-white">
        <div className="grid grid-cols-4 bg-gray-100 border-b-2 border-gray-400">
          <div className="p-3 border-r-2 border-gray-400 font-bold">Material</div>
          <div className="p-3 border-r-2 border-gray-400 font-bold">Quantidade</div>
          <div className="p-3 border-r-2 border-gray-400 font-bold">Custo Unitário</div>
          <div className="p-3 font-bold">Status</div>
        </div>

        {materiais.map((material, i) => (
          <div
            key={i}
            className={`grid grid-cols-4 border-b-2 border-gray-400 ${
              material.baixo ? "bg-yellow-50" : ""
            }`}
          >
            <div className="p-3 border-r-2 border-gray-400 flex items-center gap-2">
              {material.baixo && <span className="text-yellow-600">⚠️</span>}
              {material.nome}
            </div>
            <div className="p-3 border-r-2 border-gray-400">{material.quantidade}</div>
            <div className="p-3 border-r-2 border-gray-400">{material.custo}</div>
            <div className="p-3">
              {material.baixo ? (
                <span className="bg-yellow-200 border border-yellow-600 px-2 py-1 text-sm font-bold">
                  ESTOQUE BAIXO
                </span>
              ) : (
                <span className="text-green-600 font-bold">OK</span>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
