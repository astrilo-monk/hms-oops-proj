import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { createRoot } from 'react-dom/client';
import { Activity, BedDouble, CalendarDays, Check, ChevronDown, CircleDollarSign, ClipboardList, DoorOpen, LayoutDashboard, LoaderCircle, Plus, RefreshCw, Search, Settings2, Users, Wrench, X } from 'lucide-react';
import './style.css';

const API = 'http://127.0.0.1:8080/api';
const ROLES = {
  customer: { name: 'Aarav Sen', initials: 'AS', title: 'Customer', pages: ['Overview', 'Rooms', 'Bookings'] },
  receptionist: { name: 'Rohit Ghosh', initials: 'RG', title: 'Receptionist', pages: ['Overview', 'Rooms', 'Bookings', 'Guests', 'Billing', 'Maintenance'] },
  manager: { name: 'Anita Basu', initials: 'AB', title: 'Manager', pages: ['Overview', 'Rooms', 'Bookings', 'Maintenance'] },
  maintenance: { name: 'Biswajit Mondal', initials: 'BM', title: 'Maintenance', pages: ['Overview', 'Rooms', 'Maintenance'] }
};
const money = n => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(Number(n || 0));
const pretty = s => String(s || '').replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());

function App() {
  const [page, setPage] = useState('Overview');
  const [dashboard, setDashboard] = useState(null);
  const [rooms, setRooms] = useState([]);
  const [bookings, setBookings] = useState([]);
  const [guests, setGuests] = useState([]);
  const [loading, setLoading] = useState(true);
  const [apiError, setApiError] = useState('');
  const [search, setSearch] = useState('');
  const [modal, setModal] = useState('');
  const [notice, setNotice] = useState('');
  const [busy, setBusy] = useState(false);
  const [role, setRole] = useState(() => {
    const saved = localStorage.getItem('hms-role');
    return saved && ROLES[saved] ? saved : 'receptionist';
  });
  const user = ROLES[role];
  const can = action => role === 'receptionist' || (role === 'manager' && ['maintenance'].includes(action)) || (role === 'maintenance' && ['fix'].includes(action)) || (role === 'customer' && action === 'booking');

  const request = useCallback(async (path, options = {}) => {
    const res = await fetch(`${API}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(options.headers || {}) } });
    const data = res.status === 204 ? null : await res.json();
    if (!res.ok) throw new Error(data?.error || 'request failed');
    return data;
  }, []);
  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const [d, r, b, g] = await Promise.all([request('/dashboard'), request('/rooms'), request('/bookings'), request('/guests')]);
      setDashboard(d); setRooms(r); setBookings(b); setGuests(g); setApiError('');
    } catch (e) { setApiError(`${e.message}. start the java api with the provided script.`); }
    finally { setLoading(false); }
  }, [request]);
  useEffect(() => { refresh(); const id = setInterval(refresh, 15000); return () => clearInterval(id); }, [refresh]);

  const filteredRooms = useMemo(() => rooms.filter(r => `${r.number} ${r.type} ${r.status}`.toLowerCase().includes(search.toLowerCase())), [rooms, search]);
  const filteredBookings = useMemo(() => bookings.filter(b => `${b.id} ${b.guest} ${b.room} ${b.status}`.toLowerCase().includes(search.toLowerCase())), [bookings, search]);
  const runAction = async fn => { setBusy(true); setNotice(''); try { const result = await fn(); setNotice(result || 'changes saved'); setModal(''); await refresh(); } catch (e) { setNotice(e.message); } finally { setBusy(false); } };
  const checkout = booking => runAction(async () => { const inv = await request(`/checkout/${booking.id}`, { method: 'POST', body: '{}' }); const mode = window.prompt(`invoice ${inv.invoiceId} · total ${money(inv.total)}\npayment mode: cash, card or upi`); if (!mode) return `invoice ${inv.invoiceId} created and left unpaid`; let payload = { mode: mode.toLowerCase() }; if (payload.mode === 'cash') payload.amountTendered = window.prompt('cash tendered', String(inv.total)); if (payload.mode === 'card') { payload.cardNumber = window.prompt('demo card number, 16 digits') || ''; payload.cardHolder = booking.guest; } if (payload.mode === 'upi') payload.upiId = window.prompt('upi id') || ''; const p = await request(`/pay/${inv.invoiceId}`, { method: 'POST', body: JSON.stringify(payload) }); return p.success ? `payment received · ${p.receipt}` : `payment failed · ${p.receipt}`; });
  const fixRoom = room => runAction(async () => { await request('/maintenance/fix', { method: 'POST', body: JSON.stringify({ roomNumber: room.number }) }); return `room ${room.number} marked available`; });
  const switchRole = nextRole => { setRole(nextRole); localStorage.setItem('hms-role', nextRole); setPage('Overview'); setSearch(''); setModal(''); setNotice(`switched to ${ROLES[nextRole].title.toLowerCase()} view`); };
  const titleIcon = { Overview: LayoutDashboard, Rooms: BedDouble, Bookings: ClipboardList, Guests: Users, Billing: CircleDollarSign, Maintenance: Wrench };
  const Icon = titleIcon[page] || LayoutDashboard;

  return <div className="shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark">O</div><div><strong>orchid</strong><span>HOTEL MANAGEMENT</span></div></div>
      <div className="side-label">WORKSPACE</div>
      <nav>{[['Overview', LayoutDashboard], ['Rooms', BedDouble], ['Bookings', CalendarDays], ['Guests', Users], ['Billing', CircleDollarSign], ['Maintenance', Wrench]].filter(([name]) => user.pages.includes(name)).map(([name, I]) => <button key={name} className={`nav-item ${page === name ? 'active' : ''}`} onClick={() => { setPage(name); setSearch(''); }}><I size={17}/><span>{name}</span>{name === 'Maintenance' && dashboard?.maintenance > 0 && <b className="nav-count">{dashboard.maintenance}</b>}</button>)}</nav>
      <div className="sidebar-bottom"><div className="online"><span className="pulse"/><span>local java api</span><span className="online-dot"/></div><label className="profile-switcher"><span className="avatar">{user.initials}</span><span className="profile-text"><strong>{user.name}</strong><span>{user.title}</span></span><ChevronDown size={15}/><select aria-label="Switch role" value={role} onChange={e => switchRole(e.target.value)}>{Object.entries(ROLES).map(([key, value]) => <option key={key} value={key}>{value.title} · {value.name}</option>)}</select></label></div>
    </aside>
    <main className="main">
      <header className="topbar"><div className="crumb">WORKSPACE <span>/</span> <strong>{page.toUpperCase()}</strong></div><div className="top-actions"><div className="date-label"><CalendarDays size={15}/>{new Date().toLocaleDateString('en-IN',{day:'2-digit',month:'short',year:'numeric'})}</div><button className="icon-button" title="refresh data" onClick={refresh}><RefreshCw size={16}/></button><div className="avatar small">{user.initials}</div></div></header>
      <div className="content">
        {apiError && <div className="error-banner"><Activity size={17}/><div><strong>backend unavailable</strong><span>{apiError}</span></div><button className="button secondary" onClick={refresh}>retry</button></div>}
        {notice && <div className="notice"><Check size={16}/><span>{notice}</span><button onClick={() => setNotice('')}><X size={15}/></button></div>}
        <div className="page-heading"><div><div className="eyebrow">GRAND ORCHID HOTEL <span>·</span> {user.title.toUpperCase()}</div><h1>{page === 'Overview' ? `Good evening, ${user.name.split(' ')[0]}` : page}</h1><p>{page === 'Overview' ? `here’s what’s happening across your property today · ${user.title.toLowerCase()} view` : `manage your hotel's ${page.toLowerCase()} in one place`}</p></div>{((page === 'Guests' && role === 'receptionist') || (page === 'Maintenance' && can('maintenance')) || (page === 'Bookings' && can('booking'))) && <button className="button primary" onClick={() => setModal(page === 'Guests' ? 'guest' : page === 'Maintenance' ? 'maintenance' : 'booking')}><Plus size={16}/>{page === 'Guests' ? 'Add guest' : page === 'Maintenance' ? 'Report issue' : 'New booking'}</button>}</div>
        {page === 'Overview' && <>
          <div className="stats-grid"><Stat icon={DoorOpen} label="Total rooms" value={dashboard?.totalRooms ?? '—'} foot="across all room types"/><Stat icon={Check} label="Available" value={dashboard?.available ?? '—'} foot="ready for check-in" tone="green"/><Stat icon={BedDouble} label="Occupied" value={dashboard?.occupied ?? '—'} foot={`${dashboard?.occupancy ?? 0}% occupancy`} tone="blue"/><Stat icon={Wrench} label="Maintenance" value={dashboard?.maintenance ?? '—'} foot="rooms out of service" tone="amber"/></div>
          <div className="overview-grid"><section className="panel room-panel"><div className="panel-head"><div><h2>Room status</h2><p>live room availability</p></div><button className="text-button" onClick={() => setPage('Rooms')}>all rooms <span>↗</span></button></div><div className="room-list">{rooms.slice(0,5).map(r => <RoomRow key={r.number} room={r}/>)}</div></section><section className="panel"><div className="panel-head"><div><h2>Recent bookings</h2><p>latest reservations</p></div><button className="text-button" onClick={() => setPage('Bookings')}>view all <span>↗</span></button></div><div className="mini-bookings">{bookings.slice(-4).reverse().map(b => <div className="mini-booking" key={b.id}><div className="guest-avatar">{b.guest.split(' ').map(x=>x[0]).join('').slice(0,2)}</div><div className="mini-booking-main"><strong>{b.guest}</strong><span>room {b.room} · {b.nights} nights</span></div><div className="mini-booking-price">{money(b.amount)}<span>{b.id}</span></div></div>)}{!bookings.length && <Empty text="no bookings yet"/>}</div></section></div>
          <section className="panel bottom-panel"><div className="panel-head"><div><h2>Property snapshot</h2><p>current occupancy breakdown</p></div><span className="live-tag"><span/>LIVE</span></div><div className="occupancy"><div className="occupancy-bar"><span style={{width:`${dashboard?.occupancy || 0}%`}}/></div><div className="occupancy-foot"><span>{dashboard?.occupied ?? 0} occupied out of {dashboard?.totalRooms ?? 0} rooms</span><strong>{dashboard?.occupancy ?? 0}%</strong></div></div></section>
        </>}
        {page === 'Rooms' && <><div className="toolbar"><div className="search"><Search size={16}/><input placeholder="search rooms or status..." value={search} onChange={e=>setSearch(e.target.value)}/></div><button className="button secondary" onClick={refresh}><RefreshCw size={15}/> refresh</button></div><section className="panel table-panel"><TableHead cols={['ROOM','TYPE','FLOOR','BASE RATE / NIGHT','STATUS','AMENITIES']}/>{filteredRooms.map(r=><div className="table-row" key={r.number}><strong>#{r.number}</strong><span>{r.type}</span><span>{r.floor}</span><span>{money(r.rate)}</span><Status status={r.status}/><span className="muted amenities">{r.amenities}</span></div>)}{!filteredRooms.length && <Empty text="no matching rooms"/>}</section></>}
        {page === 'Bookings' && <><div className="toolbar"><div className="search"><Search size={16}/><input placeholder="search guest, room or booking id..." value={search} onChange={e=>setSearch(e.target.value)}/></div><button className="button secondary" onClick={refresh}><RefreshCw size={15}/> refresh</button></div><section className="panel table-panel"><TableHead cols={['BOOKING','GUEST','ROOM','STAY','NIGHTS','TOTAL','STATUS','']}/>{filteredBookings.filter(b => role !== 'customer' || b.guestId === 'G001').map(b=><div className="table-row booking-row" key={b.id}><strong>{b.id}</strong><span>{b.guest}</span><span>#{b.room}</span><span className="date-range">{b.checkIn}<br/>{b.checkOut}</span><span>{b.nights}</span><span>{money(b.amount)}</span><Status status={b.status}/>{role !== 'customer' && <button className="row-action" disabled={b.status !== 'CONFIRMED'} onClick={()=>checkout(b)}>checkout</button>}</div>)}{!filteredBookings.some(b => role !== 'customer' || b.guestId === 'G001') && <Empty text="no matching bookings"/>}</section></>}
        {page === 'Guests' && <section className="panel table-panel"><TableHead cols={['GUEST ID','NAME','PHONE','EMAIL','BOOKINGS']}/>{guests.map(g=><div className="table-row" key={g.id}><strong>{g.id}</strong><span>{g.name}</span><span>{g.phone}</span><span className="muted">{g.email}</span><span>{bookings.filter(b=>b.guestId===g.id).length}</span></div>)}{!guests.length && <Empty text="no guests registered"/>}</section>}
        {page === 'Billing' && <section className="panel table-panel"><div className="panel-head"><div><h2>Checkout & billing</h2><p>select a confirmed booking to generate an invoice and take payment</p></div></div><TableHead cols={['BOOKING','GUEST','ROOM','TOTAL WITH GST','STATUS','ACTION']}/>{bookings.map(b=><div className="table-row" key={b.id}><strong>{b.id}</strong><span>{b.guest}</span><span>#{b.room}</span><span>{money(b.amount)}</span><Status status={b.status}/><button className="row-action" disabled={b.status !== 'CONFIRMED'} onClick={()=>checkout(b)}>checkout & pay</button></div>)}{!bookings.length && <Empty text="no bookings available"/>}</section>}
        {page === 'Maintenance' && <section className="panel table-panel"><div className="panel-head"><div><h2>Maintenance board</h2><p>rooms marked unavailable until the issue is resolved</p></div></div><TableHead cols={['ROOM','TYPE','ISSUE','REPORTED BY','STATUS','ACTION']}/>{rooms.filter(r=>r.status==='UNDER_MAINTENANCE').map(r=><div className="table-row maintenance-row" key={r.number}><strong>#{r.number}</strong><span>{r.type}</span><span className="muted issue-cell">{r.maintenanceIssue || 'issue not described'}</span><span className="muted">front desk</span><Status status={r.status}/>{role === 'maintenance' && <button className="row-action" onClick={()=>fixRoom(r)}>mark fixed</button>}</div>)}{!rooms.some(r=>r.status==='UNDER_MAINTENANCE') && <Empty text="no active maintenance issues"/>}</section>}
        <footer className="footer"><span>ORCHID HOTEL MANAGEMENT</span><span>core java · in-memory · staff workspace</span></footer>
      </div>
    </main>
    {modal && <Modal title={modal==='guest'?'register guest':modal==='maintenance'?'report maintenance issue':'create booking'} close={()=>setModal('')}><ActionForm type={modal} guests={guests} rooms={rooms} busy={busy} close={()=>setModal('')} submit={data => runAction(async()=>{ if(modal==='guest'){const g=await request('/guests',{method:'POST',body:JSON.stringify(data)});return `guest registered · ${g.id}`;} if(modal==='maintenance'){await request('/maintenance',{method:'POST',body:JSON.stringify(data)});return `room ${data.roomNumber} marked for maintenance`;} const b=await request('/bookings',{method:'POST',body:JSON.stringify(data)});return `booking created · ${b.id}`;})}/></Modal>}
    {loading && <div className="loading"><LoaderCircle size={16}/> syncing</div>}
  </div>;
}
function Stat({icon:Icon,label,value,foot,tone=''}) { return <div className="stat-card"><div className={`stat-icon ${tone}`}><Icon size={18}/></div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-foot">{foot}</div></div>; }
function Status({status}) { const cls = status==='AVAILABLE'?'available':status==='OCCUPIED'||status==='CONFIRMED'?'occupied':status==='UNDER_MAINTENANCE'?'maintenance':status==='COMPLETED'?'completed':'neutral'; return <span className={`status ${cls}`}><i/>{pretty(status)}</span>; }
function RoomRow({room:r}) { return <div className="room-row"><div className="room-number">{r.number}</div><div className="room-info"><strong>{r.type} room</strong><span>floor {r.floor} · {money(r.rate)} / night</span></div><Status status={r.status}/></div>; }
function TableHead({cols}) { return <div className="table-head">{cols.map(c=><span key={c}>{c}</span>)}</div>; }
function Empty({text}) { return <div className="empty"><div className="empty-icon"><Settings2 size={19}/></div><span>{text}</span></div>; }
function Modal({title,close,children}) { return <div className="modal-backdrop" onMouseDown={e=>{if(e.target===e.currentTarget)close();}}><div className="modal"><div className="modal-head"><div><span className="eyebrow">FRONT DESK</span><h2>{title}</h2></div><button className="icon-button" onClick={close}><X size={17}/></button></div>{children}</div></div>; }
function ActionForm({type,guests,rooms,busy,submit,close}) {
 const [form,setForm] = useState({guestId:guests[0]?.id||'',roomNumber:rooms.find(r=>r.status==='AVAILABLE')?.number||'',nights:1,name:'',phone:'',email:'',idProof:'',issue:''});
 useEffect(()=>setForm(f=>({...f,guestId:f.guestId||guests[0]?.id||'',roomNumber:f.roomNumber||rooms.find(r=>r.status==='AVAILABLE')?.number||''})),[guests,rooms]);
 const set=(k,v)=>setForm(f=>({...f,[k]:v}));
 return <form className="action-form" onSubmit={e=>{e.preventDefault(); if(type==='guest')submit({name:form.name,phone:form.phone,email:form.email,idProof:form.idProof}); else if(type==='maintenance')submit({roomNumber:Number(form.roomNumber),issue:form.issue}); else submit({guestId:form.guestId,roomNumber:Number(form.roomNumber),nights:Number(form.nights)});}}>
 {type==='guest' && <><label>full name<input required value={form.name} onChange={e=>set('name',e.target.value)} placeholder="guest name"/></label><label>phone<input required pattern="[0-9]{10}" inputMode="numeric" maxLength="10" value={form.phone} onChange={e=>set('phone',e.target.value)} placeholder="10-digit phone number"/></label><label>email<input required type="email" value={form.email} onChange={e=>set('email',e.target.value)} placeholder="name@example.com"/></label><label>id proof<input value={form.idProof} onChange={e=>set('idProof',e.target.value)} placeholder="optional reference"/></label></>}
 {type==='maintenance' && <><label>room<select required value={form.roomNumber} onChange={e=>set('roomNumber',e.target.value)}>{rooms.filter(r=>r.status!=='OCCUPIED').map(r=><option key={r.number} value={r.number}>#{r.number} · {r.type} · {pretty(r.status)}</option>)}</select></label><label>describe the issue<textarea required minLength={3} value={form.issue} onChange={e=>set('issue',e.target.value)} placeholder="what needs fixing?"/></label></>}
 {type==='booking' && <><label>guest<select required value={form.guestId} onChange={e=>set('guestId',e.target.value)}>{guests.map(g=><option key={g.id} value={g.id}>{g.name} · {g.id}</option>)}</select></label><label>available room<select required value={form.roomNumber} onChange={e=>set('roomNumber',e.target.value)}>{rooms.filter(r=>r.status==='AVAILABLE').map(r=><option key={r.number} value={r.number}>#{r.number} · {r.type} · {money(r.rate)}/night</option>)}</select></label><label>number of nights<input type="number" min="1" max="365" required value={form.nights} onChange={e=>set('nights',e.target.value)}/></label><p className="form-note">check-in starts today · the java backend validates availability</p></>}
 <div className="form-actions"><button type="button" className="button secondary" onClick={close}>cancel</button><button className="button primary" disabled={busy}>{busy?'saving…':type==='guest'?'register guest':type==='maintenance'?'report issue':'confirm booking'}</button></div>
 </form>;
}

createRoot(document.getElementById('root')).render(<App/>);
