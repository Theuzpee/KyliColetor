import React, { useState, useEffect } from 'react';
import './index.css';

// SVGs (Heroicons/Lucide equivalents)
const Icons = {
  Activity: () => <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>,
  AlertTriangle: () => <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>,
  CheckCircle: () => <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>,
  Package: () => <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><line x1="16.5" y1="9.4" x2="7.5" y2="4.21"></line><path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path><polyline points="3.27 6.96 12 12.01 20.73 6.96"></polyline><line x1="12" y1="22.08" x2="12" y2="12"></line></svg>
};

// Mock Data
const MOCK_METRICS = {
  activeBoxes: 14,
  piecesPerHour: 482,
  completedToday: 1850,
  divergencesToday: 8
};

const MOCK_DIVERGENCES = [
  { id: 1, papeleta: 'PAP-DEFEITO-001', order: 'PED-1003', operator: 'João Silva', reason: 'SUJA', time: '10:45 AM', photoUrl: 'https://images.unsplash.com/photo-1605513524006-063fbdeba619?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80' },
  { id: 2, papeleta: 'PAP-DEFEITO-002', order: 'PED-1044', operator: 'Maria Clara', reason: 'AMASSADA', time: '11:12 AM', photoUrl: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?ixlib=rb-4.0.3&auto=format&fit=crop&w=150&q=80' },
  { id: 3, papeleta: 'PAP-FALTA-001', order: 'PED-1088', operator: 'Carlos Mendes', reason: 'DESABASTECIDO', time: '14:20 PM', photoUrl: null }
];

const MOCK_BOXES = [
  { id: 'PAP-MULTI-001', order: 'PED-1004', operator: 'Ana Souza', progress: 85, status: 'Em Coleta' },
  { id: 'PAP-NORMAL-005', order: 'PED-1008', operator: 'João Silva', progress: 40, status: 'Em Coleta' },
  { id: 'PAP-URGENTE-01', order: 'PED-9999', operator: 'Marcos T.', progress: 10, status: 'Parcial' }
];

function App() {
  const [currentTime, setCurrentTime] = useState(new Date().toLocaleTimeString());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date().toLocaleTimeString()), 1000);
    return () => clearInterval(timer);
  }, []);

  return (
    <div style={{ flex: 1 }}>
      <header className="glass-header animate-fade-in">
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{ background: 'var(--primary)', padding: '8px', borderRadius: '8px', display: 'flex' }}>
            <Icons.Activity />
          </div>
          <h1 className="title">APS Supervisor</h1>
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
              <span style={{ color: 'var(--primary)' }}><Icons.Package /></span>
            </div>
            <span className="metric-value">{MOCK_METRICS.activeBoxes}</span>
          </div>
          
          <div className="glass-panel metric-card animate-fade-in delay-1">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span className="metric-label">Peças / Hora (Média)</span>
              <span style={{ color: 'var(--success)' }}><Icons.Activity /></span>
            </div>
            <span className="metric-value">{MOCK_METRICS.piecesPerHour}</span>
          </div>

          <div className="glass-panel metric-card animate-fade-in delay-2">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span className="metric-label">Coletadas Hoje</span>
              <span style={{ color: 'var(--success)' }}><Icons.CheckCircle /></span>
            </div>
            <span className="metric-value" style={{ color: 'var(--success)' }}>{MOCK_METRICS.completedToday}</span>
          </div>

          <div className="glass-panel metric-card animate-fade-in delay-2">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
              <span className="metric-label">Divergências</span>
              <span style={{ color: 'var(--danger)' }}><Icons.AlertTriangle /></span>
            </div>
            <span className="metric-value" style={{ color: 'var(--danger)' }}>{MOCK_METRICS.divergencesToday}</span>
          </div>
        </section>

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
                  {MOCK_DIVERGENCES.map((div) => (
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
              {MOCK_BOXES.map((box) => (
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
