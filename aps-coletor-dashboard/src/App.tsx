import React, { useState, useEffect } from 'react';
import { AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { Activity, AlertTriangle, CheckCircle, Package, MapPin, Users } from 'lucide-react';
import './index.css';
import './index.css';

// Remove old Icons constant as we are using lucide-react now

// Mock Data
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
  const [telemetry, setTelemetry] = useState<any>(null);
  const [divergencesSummary, setDivergencesSummary] = useState<any[]>([]);

  useEffect(() => {
    fetch('/api/picking/divergences/summary')
      .then(res => res.json())
      .then(data => {
        if (data.byReason) {
           const chartData = Object.entries(data.byReason).map(([name, value]) => ({ name, value }));
           setDivergencesSummary(chartData.length > 0 ? chartData : [
             { name: 'Desabastecido', value: 5 },
             { name: 'Código Ilegível', value: 2 },
             { name: 'Caixa Avariada', value: 1 }
           ]); // Mock de fallback se vier vazio
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

  return (
    <div style={{ flex: 1 }}>
      <header className="glass-header animate-fade-in">
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

      <main className="dashboard-container">
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
                  {recentDivergences.map((div: any) => (
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
              {activeBoxesList.map((box: any) => (
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
      </main>
    </div>
  );
}

export default App;
