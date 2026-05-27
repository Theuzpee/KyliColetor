import React, { useState, useEffect } from 'react';
import { AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Activity, AlertTriangle, CheckCircle, Package, Users, ClipboardList, Clock, Barcode as BarcodeIcon } from 'lucide-react';

interface ActiveBox {
  id: string;
  order: string;
  status: string;
  progress: number;
  operator: string;
  totalCollected: number;
  totalRequired: number;
}

interface CompletedBox {
  id: string;
  order: string;
  operator: string;
  totalCollected: number;
  totalRequired: number;
  time: string;
}

interface Divergence {
  id: string | number;
  photoUrl: string | null;
  papeleta: string;
  reason: string;
  operator: string;
  time: string;
}

interface DivergenceSummaryItem {
  name: string;
  value: number;
}

interface TelemetryMetrics {
  activeBoxes: number;
  piecesPerHour: number;
  completedToday: number;
  divergencesToday: number;
}

interface TelemetryProductivityItem {
  time: string;
  pieces: number;
}

interface TelemetryData {
  metrics: TelemetryMetrics;
  productivity: TelemetryProductivityItem[];
  recentDivergences: Divergence[];
  activeBoxesList: ActiveBox[];
  completedBoxesList: CompletedBox[];
}
import './index.css';

// SVG Barcode Generator (Code 39)
const Barcode: React.FC<{ value: string; height?: number }> = ({ value, height = 60 }) => {
  const encodings: Record<string, string> = {
    '0': 'N N W W N N N W N',
    '1': 'W N N W N N N N W',
    '2': 'N N W W N N N N W',
    '3': 'W N W W N N N N N',
    '4': 'N N N W W N N N W',
    '5': 'W N N W W N N N N',
    '6': 'N N W W W N N N N',
    '7': 'N N N W N N W N W',
    '8': 'W N N W N N W N N',
    '9': 'N N W W N N W N N',
    'A': 'W N N N N W N N W',
    'B': 'N N W N N W N N W',
    'C': 'W N W N N W N N N',
    'D': 'N N N N W W N N W',
    'E': 'W N N N W W N N N',
    'F': 'N N W N W W N N N',
    'G': 'N N N N N W W N W',
    'H': 'W N N N N W W N N',
    'I': 'N N W N N W W N N',
    'J': 'N N N N W W W N N',
    'K': 'W N N N N N N W W',
    'L': 'N N N N W W N W N',
    'M': 'W N W N N N N W N',
    'N': 'N N N N W N N W W',
    'O': 'W N N N N W W N N',
    'P': 'N N W N W N N W N',
    'Q': 'N N N N N N W W W',
    'R': 'W N N N N N W W N',
    'S': 'N N W N N N W W N',
    'T': 'N N N N W N W W N',
    'U': 'W W N N N N N N W',
    'V': 'N W W N N N N N W',
    'W': 'W W W N N N N N N',
    'X': 'N W N N W N N N W',
    'Y': 'W W N N W N N N N',
    'Z': 'N W W N W N N N N',
    '-': 'N W N N N N W N W',
    '.': 'W W N N N N W N N',
    ' ': 'N W W N N N W N N',
    '*': 'N W N N W N W N N'
  };

  const formattedValue = `*${value.toUpperCase()}*`;
  let currentX = 0;
  const bars: { x: number; width: number }[] = [];

  for (let i = 0; i < formattedValue.length; i++) {
    const char = formattedValue[i];
    const encoding = encodings[char] || encodings[' '];
    const elements = encoding.split(' ');

    elements.forEach((element, index) => {
      const width = element === 'W' ? 3 : 1;
      const isBar = index % 2 === 0;

      if (isBar) {
        bars.push({ x: currentX, width });
      }
      currentX += width;
    });

    if (i < formattedValue.length - 1) {
      currentX += 1;
    }
  }

  return (
    <svg 
      width="100%" 
      height={height} 
      viewBox={`0 0 ${currentX} ${height}`} 
      style={{ 
        display: 'block', 
        background: '#fff', 
        padding: '4px',
        shapeRendering: 'crispEdges',
        maxWidth: '360px',
        margin: '0 auto'
      }}
    >
      {bars.map((bar, idx) => (
        <rect 
          key={idx} 
          x={bar.x} 
          y={0} 
          width={bar.width} 
          height={height} 
          fill="#000" 
        />
      ))}
    </svg>
  );
};

// Mock Data as fallback
const MOCK_PRODUCTIVITY_DATA = [
  { time: '08:00', pieces: 120 },
  { time: '09:00', pieces: 340 },
  { time: '10:00', pieces: 480 },
  { time: '11:00', pieces: 520 },
  { time: '12:00', pieces: 210 },
  { time: '13:00', pieces: 450 },
  { time: '14:00', pieces: 600 },
];

function App() {
  const [currentTime, setCurrentTime] = useState(new Date().toLocaleTimeString());
  const [telemetry, setTelemetry] = useState<TelemetryData | null>(null);
  const [divergencesSummary, setDivergencesSummary] = useState<DivergenceSummaryItem[]>([]);
  const [activeTab, setActiveTab] = useState<'telemetry' | 'picking'>('telemetry');
  const [selectedBarcodeBox, setSelectedBarcodeBox] = useState<ActiveBox | CompletedBox | null>(null);

  useEffect(() => {
    fetch('/api/picking/divergences/summary')
      .then(res => res.json())
      .then(data => {
        if (data.byReason) {
           const chartData = Object.entries(data.byReason).map(([name, value]) => ({ name, value: Number(value) }));
           setDivergencesSummary(chartData.length > 0 ? chartData : [
             { name: 'Desabastecido', value: 5 },
             { name: 'Código Ilegível', value: 2 },
             { name: 'Caixa Avariada', value: 1 }
           ]); 
        }
      })
      .catch(err => console.error('API offline', err));

    const sse = new EventSource('/api/picking/telemetry/stream');
    
    sse.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        setTelemetry(data);
      } catch (err) {
        console.error('Erro ao fazer parse do SSE', err);
      }
    };
    
    sse.onerror = (err) => {
      console.error('Erro na conexão SSE', err);
    };

    const timer = setInterval(() => {
      setCurrentTime(new Date().toLocaleTimeString());
    }, 1000);

    return () => {
      clearInterval(timer);
      sse.close();
    };
  }, []);

  const activeBoxesCount = telemetry?.metrics?.activeBoxes ?? 0;
  const piecesPerHour = telemetry?.metrics?.piecesPerHour ?? 0;
  const completedToday = telemetry?.metrics?.completedToday ?? 0;
  const divergencesToday = telemetry?.metrics?.divergencesToday ?? 0;
  
  const productivityData = telemetry?.productivity ?? MOCK_PRODUCTIVITY_DATA;
  const recentDivergences = telemetry?.recentDivergences ?? [];
  const activeBoxesList = telemetry?.activeBoxesList ?? [];
  const completedBoxesList = telemetry?.completedBoxesList ?? [];

  return (
    <div style={{ flex: 1, minHeight: '100vh', background: 'var(--bg)', color: '#fff' }}>
      <header className="glass-header animate-fade-in" style={{ marginBottom: '16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ background: 'var(--primary)', padding: '8px', borderRadius: '8px', display: 'flex' }}>
            <Activity color="#fff" size={24} />
          </div>
          <h1 className="title">APS Dashboard Operacional</h1>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
          <div className="badge primary">🟢 Real-time Sync Active</div>
          <div style={{ color: 'var(--text-muted)', fontSize: '14px', fontWeight: 500 }}>
            {currentTime}
          </div>
        </div>
      </header>

      {/* Tab Navigation */}
      <div style={{ display: 'flex', gap: '12px', padding: '0 32px', marginBottom: '24px' }} className="animate-fade-in delay-1">
        <button
          onClick={() => setActiveTab('telemetry')}
          style={{
            background: activeTab === 'telemetry' ? 'var(--primary)' : 'rgba(255, 255, 255, 0.03)',
            border: activeTab === 'telemetry' ? '1px solid var(--primary)' : '1px solid rgba(255, 255, 255, 0.08)',
            color: activeTab === 'telemetry' ? '#fff' : 'var(--text-muted)',
            padding: '10px 20px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            transition: 'all 0.3s ease',
            boxShadow: activeTab === 'telemetry' ? '0 0 20px rgba(59, 130, 246, 0.15)' : 'none'
          }}
        >
          <Activity size={16} />
          Painel Geral
        </button>
        <button
          onClick={() => setActiveTab('picking')}
          style={{
            background: activeTab === 'picking' ? 'var(--primary)' : 'rgba(255, 255, 255, 0.03)',
            border: activeTab === 'picking' ? '1px solid var(--primary)' : '1px solid rgba(255, 255, 255, 0.08)',
            color: activeTab === 'picking' ? '#fff' : 'var(--text-muted)',
            padding: '10px 20px',
            borderRadius: '8px',
            cursor: 'pointer',
            fontWeight: 600,
            fontSize: '14px',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            transition: 'all 0.3s ease',
            boxShadow: activeTab === 'picking' ? '0 0 20px rgba(59, 130, 246, 0.15)' : 'none'
          }}
        >
          <ClipboardList size={16} />
          Monitor de Picking
        </button>
      </div>

      <main className="dashboard-container">
        {activeTab === 'telemetry' ? (
          <>
            {/* Metrics Row */}
            <section className="metrics-grid">
              <div className="glass-panel metric-card animate-fade-in delay-1">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span className="metric-label">Caixas Ativas</span>
                  <span style={{ color: 'var(--primary)' }}><Package size={24} /></span>
                </div>
                <span className="metric-value">{activeBoxesCount}</span>
              </div>
              
              <div className="glass-panel metric-card animate-fade-in delay-1">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span className="metric-label">Peças / Hora (Média)</span>
                  <span style={{ color: 'var(--success)' }}><Activity size={24} /></span>
                </div>
                <span className="metric-value">{piecesPerHour}</span>
              </div>

              <div className="glass-panel metric-card animate-fade-in delay-2">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span className="metric-label">Coletadas Hoje</span>
                  <span style={{ color: 'var(--success)' }}><CheckCircle size={24} /></span>
                </div>
                <span className="metric-value" style={{ color: 'var(--success)' }}>{completedToday}</span>
              </div>

              <div className="glass-panel metric-card animate-fade-in delay-2">
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <span className="metric-label">Divergências</span>
                  <span style={{ color: 'var(--danger)' }}><AlertTriangle size={24} /></span>
                </div>
                <span className="metric-value" style={{ color: 'var(--danger)' }}>{divergencesToday}</span>
              </div>
            </section>

            {/* Charts Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px', marginBottom: '32px' }}>
              
              {/* Productivity Chart Section */}
              <section className="glass-panel animate-fade-in delay-3" style={{ padding: '24px', height: '350px' }}>
                <h2 style={{ fontSize: '18px', marginBottom: '24px', fontWeight: 600 }}>Produtividade (Peças por Hora)</h2>
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={productivityData} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                    <defs>
                      <linearGradient id="colorPieces" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                        <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                      </linearGradient>
                    </defs>
                    <XAxis dataKey="time" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                    <YAxis stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" vertical={false} />
                    <Tooltip 
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px' }}
                      itemStyle={{ color: '#fff' }}
                    />
                    <Area type="monotone" dataKey="pieces" stroke="#3b82f6" strokeWidth={3} fillOpacity={1} fill="url(#colorPieces)" />
                  </AreaChart>
                </ResponsiveContainer>
              </section>

              {/* Divergences Heatmap (BarChart) */}
              <section className="glass-panel animate-fade-in delay-3" style={{ padding: '24px', height: '350px' }}>
                <h2 style={{ fontSize: '18px', marginBottom: '24px', fontWeight: 600 }}>Causa Raiz de Paradas</h2>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={divergencesSummary} layout="vertical" margin={{ top: 10, right: 30, left: 40, bottom: 0 }}>
                    <XAxis type="number" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                    <YAxis dataKey="name" type="category" stroke="#94a3b8" fontSize={11} tickLine={false} axisLine={false} />
                    <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" horizontal={false} />
                    <Tooltip 
                      cursor={{fill: 'rgba(255,255,255,0.05)'}}
                      contentStyle={{ backgroundColor: 'rgba(15, 23, 42, 0.9)', border: '1px solid rgba(255,255,255,0.1)', borderRadius: '8px' }}
                      itemStyle={{ color: '#fff' }}
                    />
                    <Bar dataKey="value" fill="#f59e0b" radius={[0, 4, 4, 0]} barSize={24} />
                  </BarChart>
                </ResponsiveContainer>
              </section>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '32px' }}>
              {/* Divergences Table */}
              <section className="glass-panel animate-fade-in delay-3" style={{ padding: '24px' }}>
                <h2 style={{ fontSize: '18px', marginBottom: '24px', fontWeight: 600 }}>Últimas Divergências Registradas</h2>
                <div className="table-container">
                  <table>
                    <thead>
                      <tr>
                        <th>Evidência</th>
                        <th>Papeleta</th>
                        <th>Motivo</th>
                        <th>Operador</th>
                        <th>Horário</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recentDivergences.map((div: Divergence) => (
                        <tr key={div.id}>
                          <td>
                            {div.photoUrl ? (
                              <img src={div.photoUrl} alt="Evidência" className="photo-thumbnail" />
                            ) : (
                              <div className="no-photo">N/A</div>
                            )}
                          </td>
                          <td style={{ fontWeight: 500 }}>{div.papeleta}</td>
                          <td>
                            <span className={`badge ${div.reason === 'DESABASTECIDO' ? 'warning' : 'danger'}`}>
                              {div.reason}
                            </span>
                          </td>
                          <td style={{ color: 'var(--text-muted)' }}>{div.operator}</td>
                          <td style={{ color: 'var(--text-muted)' }}>{div.time}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>

              {/* Active Boxes List */}
              <section className="glass-panel animate-fade-in delay-3" style={{ padding: '24px' }}>
                <h2 style={{ fontSize: '18px', marginBottom: '24px', fontWeight: 600 }}>Caixas em Andamento</h2>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                  {activeBoxesList.map((box: ActiveBox) => (
                    <div key={box.id} style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <span style={{ fontWeight: 500, fontSize: '15px' }}>{box.id}</span>
                        <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>{box.progress}%</span>
                      </div>
                      <div style={{ width: '100%', height: '6px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                        <div style={{ width: `${box.progress}%`, height: '100%', background: box.progress > 50 ? 'var(--success)' : 'var(--primary)' }}></div>
                      </div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px', color: 'var(--text-muted)' }}>
                        <span>{box.operator}</span>
                        <span>{box.status}</span>
                      </div>
                    </div>
                  ))}
                </div>
              </section>
            </div>
          </>
        ) : (
          /* Active Tab: Picking Monitor (Pending vs Scanned) */
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px' }} className="animate-fade-in">
            
            {/* Column Left: Pending Papeletas (In Progress) */}
            <section className="glass-panel" style={{ padding: '24px', minHeight: '550px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <Package size={20} color="var(--primary)" />
                  Papeletas em Andamento (Pendentes)
                </h2>
                <div className="badge primary" style={{ fontSize: '13px', fontWeight: 600 }}>
                  {activeBoxesList.length} Ativas
                </div>
              </div>

              {activeBoxesList.length === 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '400px', gap: '16px', color: 'var(--text-muted)' }}>
                  <CheckCircle size={48} color="var(--success)" />
                  <span>Sem coletas pendentes no momento.</span>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {activeBoxesList.map((box: ActiveBox) => (
                    <div 
                      key={box.id} 
                      className="glass-panel" 
                      style={{ 
                        padding: '16px', 
                        border: '1px solid rgba(255,255,255,0.06)', 
                        background: 'rgba(255,255,255,0.02)',
                        borderRadius: '12px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '12px'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <span style={{ fontWeight: 600, fontSize: '16px', color: '#fff', fontFamily: 'monospace' }}>{box.id}</span>
                          <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
                            Pedido: <strong style={{ color: '#fff' }}>{box.order}</strong>
                          </div>
                        </div>
                        <span 
                          className={`badge ${box.status === 'Parcial' ? 'warning' : box.status === 'Multi-Andar' ? 'primary' : 'success'}`}
                          style={{ fontSize: '11px', fontWeight: 700 }}
                        >
                          {box.status}
                        </span>
                      </div>

                      {/* Progress bar */}
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '12px' }}>
                          <span style={{ color: 'var(--text-muted)' }}>Progresso da Coleta</span>
                          <span style={{ fontWeight: 600, color: 'var(--primary)' }}>{box.progress}%</span>
                        </div>
                        <div style={{ width: '100%', height: '8px', background: 'rgba(255,255,255,0.05)', borderRadius: '4px', overflow: 'hidden' }}>
                          <div 
                            style={{ 
                              width: `${box.progress}%`, 
                              height: '100%', 
                              background: box.progress > 70 ? 'var(--success)' : 'var(--primary)',
                              transition: 'width 0.5s ease-in-out'
                            }}
                          ></div>
                        </div>
                      </div>

                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '13px', paddingTop: '4px', borderTop: '1px solid rgba(255,255,255,0.04)', marginBottom: '12px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)' }}>
                          <Users size={14} />
                          <span>{box.operator}</span>
                        </div>
                        <div style={{ fontWeight: 500 }}>
                          {box.totalCollected} / {box.totalRequired} <span style={{ color: 'var(--text-muted)', fontSize: '12px' }}>peças</span>
                        </div>
                      </div>

                      <button
                        onClick={() => setSelectedBarcodeBox(box)}
                        style={{
                          width: '100%',
                          background: 'rgba(59, 130, 246, 0.08)',
                          border: '1px solid rgba(59, 130, 246, 0.2)',
                          color: '#60a5fa',
                          padding: '8px 12px',
                          borderRadius: '8px',
                          cursor: 'pointer',
                          fontWeight: 600,
                          fontSize: '12px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          gap: '8px',
                          transition: 'all 0.2s ease',
                        }}
                      >
                        <BarcodeIcon size={14} />
                        Código de Barras
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>

            {/* Column Right: Scanned Today (Completed) */}
            <section className="glass-panel" style={{ padding: '24px', minHeight: '550px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px' }}>
                <h2 style={{ fontSize: '18px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <CheckCircle size={20} color="var(--success)" />
                  Papeletas Bipadas Hoje (Concluídas)
                </h2>
                <div className="badge success" style={{ fontSize: '13px', fontWeight: 600 }}>
                  {completedBoxesList.length} Finalizadas
                </div>
              </div>

              {completedBoxesList.length === 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '400px', gap: '16px', color: 'var(--text-muted)' }}>
                  <Clock size={48} />
                  <span>Nenhuma papeleta concluída hoje ainda.</span>
                </div>
              ) : (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                  {completedBoxesList.map((box: CompletedBox) => (
                    <div 
                      key={box.id} 
                      className="glass-panel" 
                      style={{ 
                        padding: '16px', 
                        border: '1px solid rgba(76, 175, 80, 0.15)', 
                        background: 'rgba(76, 175, 80, 0.02)',
                        borderRadius: '12px',
                        display: 'flex',
                        flexDirection: 'column',
                        gap: '12px'
                      }}
                    >
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <div>
                          <span style={{ fontWeight: 600, fontSize: '16px', color: 'var(--success)', fontFamily: 'monospace', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            <CheckCircle size={16} />
                            {box.id}
                          </span>
                          <div style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '2px' }}>
                            Pedido: <strong style={{ color: '#fff' }}>{box.order}</strong>
                          </div>
                        </div>
                        <span 
                          className="badge success" 
                          style={{ fontSize: '11px', fontWeight: 700 }}
                        >
                          Concluída
                        </span>
                      </div>

                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '13px', paddingTop: '8px', borderTop: '1px solid rgba(255,255,255,0.04)', marginBottom: '12px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--text-muted)' }}>
                          <Users size={14} />
                          <span>{box.operator}</span>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span style={{ color: 'var(--success)', fontWeight: 600 }}>{box.totalCollected} / {box.totalRequired} peças</span>
                          <span style={{ color: 'var(--text-muted)', fontSize: '12px' }}>•</span>
                          <span style={{ color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
                            <Clock size={12} />
                            {box.time}
                          </span>
                        </div>
                      </div>

                      <button
                        onClick={() => setSelectedBarcodeBox(box)}
                        style={{
                          width: '100%',
                          background: 'rgba(16, 185, 129, 0.08)',
                          border: '1px solid rgba(16, 185, 129, 0.2)',
                          color: '#34d399',
                          padding: '8px 12px',
                          borderRadius: '8px',
                          cursor: 'pointer',
                          fontWeight: 600,
                          fontSize: '12px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          gap: '8px',
                          transition: 'all 0.2s ease',
                        }}
                      >
                        <BarcodeIcon size={14} />
                        Código de Barras
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </section>
            
          </div>
        )}
      </main>

      {/* Modal de Código de Barras */}
      {selectedBarcodeBox && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(15, 23, 42, 0.75)',
          backdropFilter: 'blur(8px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 9999,
          animation: 'fadeIn 0.2s ease-out'
        }}>
          <div style={{
            background: '#1e293b',
            border: '1px solid rgba(255, 255, 255, 0.1)',
            borderRadius: '16px',
            padding: '32px',
            width: '450px',
            maxWidth: '90%',
            boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: '24px',
            animation: 'slideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
              <h3 style={{ fontSize: '20px', fontWeight: 700, color: '#fff', margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                <BarcodeIcon size={20} color="var(--primary)" />
                Código da Papeleta
              </h3>
              <button 
                onClick={() => setSelectedBarcodeBox(null)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--text-muted)',
                  cursor: 'pointer',
                  fontSize: '20px',
                  lineHeight: 1,
                  padding: '4px'
                }}
              >
                &times;
              </button>
            </div>
            
            <div style={{ 
              background: '#fff', 
              padding: '24px', 
              borderRadius: '12px', 
              width: '100%',
              boxShadow: 'inset 0 2px 4px rgba(0,0,0,0.06)'
            }}>
              <Barcode value={selectedBarcodeBox.id} height={80} />
            </div>

            <div style={{ textAlign: 'center' }}>
              <div style={{ fontFamily: 'monospace', fontSize: '22px', fontWeight: 700, color: '#fff', letterSpacing: '2px' }}>
                {selectedBarcodeBox.id}
              </div>
              <div style={{ fontSize: '14px', color: 'var(--text-muted)', marginTop: '6px' }}>
                Pedido: <strong style={{ color: '#fff' }}>{selectedBarcodeBox.order}</strong>
              </div>
            </div>

            <div style={{ display: 'flex', width: '100%', gap: '12px', marginTop: '8px' }}>
              <button
                onClick={() => {
                  navigator.clipboard.writeText(selectedBarcodeBox.id);
                  alert('Código de barras copiado!');
                }}
                style={{
                  flex: 1,
                  background: 'rgba(255, 255, 255, 0.05)',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                  color: '#fff',
                  padding: '12px',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontWeight: 600,
                  transition: 'all 0.2s',
                  fontSize: '14px'
                }}
              >
                Copiar Código
              </button>
              <button
                onClick={() => setSelectedBarcodeBox(null)}
                style={{
                  flex: 1,
                  background: 'var(--primary)',
                  border: 'none',
                  color: '#fff',
                  padding: '12px',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontWeight: 600,
                  transition: 'all 0.2s',
                  fontSize: '14px'
                }}
              >
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default App;
