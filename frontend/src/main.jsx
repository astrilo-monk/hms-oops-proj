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
  const [pendingInvoice, setPendingInvoice] = useState(null);
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [role, setRole] = useState(() => {
    const saved = localStorage.getItem('hms-role');
    return saved && ROLES[saved] ? saved : 'receptionist';
  });
  const [customerName, setCustomerName] = useState(() => localStorage.getItem('hms-customer-name') || ROLES.customer.name);
  const user = role === 'customer' ? { ...ROLES.customer, name: customerName, initials: customerName.split(/\s+/).map(part => part[0]).join('').slice(0, 2).toUpperCase() } : ROLES[role];
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
  const checkout = booking => runAction(async () => {
    const invoice = await request(`/checkout/${booking.id}`, { method: 'POST', body: '{}' });
    setPendingInvoice({ ...invoice, guest: booking.guest });
    setPaymentOpen(true);
    return `invoice ${invoice.invoiceId} created; confirm payment to continue`;
  });
  const fixRoom = room => runAction(async () => { await request('/maintenance/fix', { method: 'POST', body: JSON.stringify({ roomNumber: room.number }) }); return `room ${room.number} marked available`; });
  const switchRole = nextRole => { setRole(nextRole); localStorage.setItem('hms-role', nextRole); setPage('Overview'); setSearch(''); setModal(''); setNotice(`switched to ${ROLES[nextRole].title.toLowerCase()} view`); };
  const selectRole = nextRole => { if (nextRole === '__new_guest__') { setModal('guest'); return; } switchRole(nextRole); };
  const titleIcon = { Overview: LayoutDashboard, Rooms: BedDouble, Bookings: ClipboardList, Guests: Users, Billing: CircleDollarSign, Maintenance: Wrench };
  const Icon = titleIcon[page] || LayoutDashboard;

  return <div className="shell">
    <aside className="sidebar">
      <div className="brand"><div className="brand-mark">O</div><div><strong>orchid</strong><span>HOTEL MANAGEMENT</span></div></div>
      <div className="side-label">WORKSPACE</div>
      <nav>{[['Overview', LayoutDashboard], ['Rooms', BedDouble], ['Bookings', CalendarDays], ['Guests', Users], ['Billing', CircleDollarSign], ['Maintenance', Wrench]].filter(([name]) => user.pages.includes(name)).map(([name, I]) => <button key={name} className={`nav-item ${page === name ? 'active' : ''}`} onClick={() => { setPage(name); setSearch(''); }}><I size={17}/><span>{name}</span>{name === 'Maintenance' && dashboard?.maintenance > 0 && <b className="nav-count">{dashboard.maintenance}</b>}</button>)}</nav>
      <div className="sidebar-bottom"><div className="online"><span className="pulse"/><span>local java api</span><span className="online-dot"/></div><label className="profile-switcher"><span className="avatar">{user.initials}</span><span className="profile-text"><strong>{user.name}</strong><span>{user.title}</span></span><ChevronDown size={15}/><select aria-label="Switch role" value={role} onChange={e => selectRole(e.target.value)}>{Object.entries(ROLES).map(([key, value]) => <option key={key} value={key}>{value.title} Â· {key === 'customer' ? customerName : value.name}</option>)}<option value="__new_guest__">+ New guest...</option></select></label></div>
    </aside>
    <main className="main">
      <header className="topbar"><div className="crumb">WORKSPACE <span>/</span> <strong>{page.toUpperCase()}</strong></div><div className="top-actions"><div className="date-label"><CalendarDays size={15}/>{new Date().toLocaleDateString('en-IN',{day:'2-digit',month:'short',year:'numeric'})}</div><button className="icon-button" title="refresh data" onClick={refresh}><RefreshCw size={16}/></button><div className="avatar small">{user.initials}</div></div></header>
      <div className="content">
        {apiError && <div className="error-banner"><Activity size={17}/><div><strong>backend unavailable</strong><span>{apiError}</span></div><button className="button secondary" onClick={refresh}>retry</button></div>}
        {notice && <div className="notice"><Check size={16}/><span>{notice}</span><button onClick={() => setNotice('')}><X size={15}/></button></div>}
        {pendingInvoice && !paymentOpen && <div className="notice"><CircleDollarSign size={16}/><span>Invoice {pendingInvoice.invoiceId} remains unpaid.</span><button className="text-button" onClick={() => setPaymentOpen(true)}>continue payment</button></div>}
        <div className="page-heading"><div><div className="eyebrow">GRAND ORCHID HOTEL <span>Â·</span> {user.title.toUpperCase()}</div><h1>{page === 'Overview' ? `Good evening, ${user.name.split(' ')[0]}` : page}</h1><p>{page === 'Overview' ? `hereâ€™s whatâ€™s happening across your property today Â· ${user.title.toLowerCase()} view` : `manage your hotel's ${page.toLowerCase()} in one place`}</p></div>{((page === 'Guests' && role === 'receptionist') || (page === 'Maintenance' && can('maintenance')) || (page === 'Bookings' && can('booking')) || (page === 'Overview' && role === 'manager')) && <button className="button primary" onClick={() => setModal(page === 'Guests' ? 'guest' : page === 'Maintenance' ? 'maintenance' : page === 'Overview' ? 'staff' : 'booking')}><Plus size={16}/>{page === 'Guests' ? 'Add guest' : page === 'Maintenance' ? 'Report issue' : page === 'Overview' ? 'Hire staff' : 'New booking'}</button>}</div>
        {page === 'Overview' && <>
          <div className="stats-grid"><Stat icon={DoorOpen} label="Total rooms" value={dashboard?.totalRooms ?? 'â€”'} foot="across all room types"/><Stat icon={Check} label="Available" value={dashboard?.available ?? 'â€”'} foot="ready for check-in" tone="green"/><Stat icon={BedDouble} label="Occupied" value={dashboard?.occupied ?? 'â€”'} foot={`${dashboard?.occupancy ?? 0}% occupancy`} tone="blue"/><Stat icon={Wrench} label="Maintenance" value={dashboard?.maintenance ?? 'â€”'} foot="rooms out of service" tone="amber"/></div>
          <div className="overview-grid"><section className="panel room-panel"><div className="panel-head"><div><h2>Room status</h2><p>live room availability</p></div><button className="text-button" onClick={() => setPage('Rooms')}>all rooms <span>â†—</span></button></div><div className="room-list">{rooms.slice(0,5).map(r => <RoomRow key={r.number} room={r}/>)}</div></section><section className="panel"><div className="panel-head"><div><h2>Recent bookings</h2><p>latest reservations</p></div><button className="text-button" onClick={() => setPage('Bookings')}>view all <span>â†—</span></button></div><div className="mini-bookings">{bookings.slice(-4).reverse().map(b => <div className="mini-booking" key={b.id}><div className="guest-avatar">{b.guest.split(' ').map(x=>x[0]).join('').slice(0,2)}</div><div className="mini-booking-main"><strong>{b.guest}</strong><span>room {b.room} Â· {b.nights} nights</span></div><div className="mini-booking-price">{money(b.amount)}<span>{b.id}</span></div></div>)}{!bookings.length && <Empty text="no bookings yet"/>}</div></section></div>
          <section className="panel bottom-panel"><div className="panel-head"><div><h2>Property snapshot</h2><p>current occupancy breakdown</p></div><span className="live-tag"><span/>LIVE</span></div><div className="occupancy"><div className="occupancy-bar"><span style={{width:`${dashboard?.occupancy || 0}%`}}/></div><div className="occupancy-foot"><span>{dashboard?.occupied ?? 0} occupied out of {dashboard?.totalRooms ?? 0} rooms</span><strong>{dashboard?.occupancy ?? 0}%</strong></div></div></section>
        </>}
        {page === 'Rooms' && <><div className="toolbar"><div className="search"><Search size={16}/><input placeholder="search rooms or status..." value={search} onChange={e=>setSearch(e.target.value)}/></div><button className="button secondary" onClick={refresh}><RefreshCw size={15}/> refresh</button></div><section className="panel table-panel"><TableHead cols={['ROOM','TYPE','FLOOR','BASE RATE / NIGHT','STATUS','AMENITIES']}/>{filteredRooms.map(r=><div className="table-row" key={r.number}><strong>#{r.number}</strong><span>{r.type}</span><span>{r.floor}</span><span>{money(r.rate)}</span><Status status={r.status}/><span className="muted amenities">{r.amenities}</span></div>)}{!filteredRooms.length && <Empty text="no matching rooms"/>}</section></>}
        {page === 'Bookings' && <><div className="toolbar"><div className="search"><Search size={16}/><input placeholder="search guest, room or booking id..." value={search} onChange={e=>setSearch(e.target.value)}/></div><button className="button secondary" onClick={refresh}><RefreshCw size={15}/> refresh</button></div><section className="panel table-panel"><TableHead cols={['BOOKING','GUEST','ROOM','STAY','NIGHTS','TOTAL','STATUS','']}/>{filteredBookings.filter(b => role !== 'customer' || b.guestId === (localStorage.getItem('hms-customer-id') || 'G001')).map(b=><div className="table-row booking-row" key={b.id}><strong>{b.id}</strong><span>{b.guest}</span><span>#{b.room}</span><span className="date-range">{b.checkIn}<br/>{b.checkOut}</span><span>{b.nights}</span><span>{money(b.amount)}</span><Status status={b.status}/>{role !== 'customer' && <button className="row-action" disabled={b.status !== 'CONFIRMED'} onClick={()=>checkout(b)}>checkout</button>}</div>)}{!filteredBookings.some(b => role !== 'customer' || b.guestId === (localStorage.getItem('hms-customer-id') || 'G001')) && <Empty text="no matching bookings"/>}</section></>}
        {page === 'Guests' && <section className="panel table-panel"><TableHead cols={['GUEST ID','NAME','PHONE','EMAIL','BOOKINGS']}/>{guests.map(g=><div className="table-row" key={g.id}><strong>{g.id}</strong><span>{g.name}</span><span>{g.phone}</span><span className="muted">{g.email}</span><span>{bookings.filter(b=>b.guestId===g.id).length}</span></div>)}{!guests.length && <Empty text="no guests registered"/>}</section>}
        {page === 'Billing' && <section className="panel table-panel"><div className="panel-head"><div><h2>Checkout & billing</h2><p>select a confirmed booking to generate an invoice and take payment</p></div></div><TableHead cols={['BOOKING','GUEST','ROOM','TOTAL WITH GST','STATUS','ACTION']}/>{bookings.map(b=><div className="table-row" key={b.id}><strong>{b.id}</strong><span>{b.guest}</span><span>#{b.room}</span><span>{money(b.amount)}</span><Status status={b.status}/><button className="row-action" disabled={b.status !== 'CONFIRMED'} onClick={()=>checkout(b)}>checkout & pay</button></div>)}{!bookings.length && <Empty text="no bookings available"/>}</section>}
        {page === 'Maintenance' && <section className="panel table-panel"><div className="panel-head"><div><h2>Maintenance board</h2><p>rooms marked unavailable until the issue is resolved</p></div></div><TableHead cols={['ROOM','TYPE','ISSUE','REPORTED BY','STATUS','ACTION']}/>{rooms.filter(r=>r.status==='UNDER_MAINTENANCE').map(r=><div className="table-row maintenance-row" key={r.number}><strong>#{r.number}</strong><span>{r.type}</span><span className="muted issue-cell">{r.maintenanceIssue || 'issue not described'}</span><span className="muted">front desk</span><Status status={r.status}/>{role === 'maintenance' && <button className="row-action" onClick={()=>fixRoom(r)}>mark fixed</button>}</div>)}{!rooms.some(r=>r.status==='UNDER_MAINTENANCE') && <Empty text="no active maintenance issues"/>}</section>}
        <footer className="footer"><span>ORCHID HOTEL MANAGEMENT</span><span>core java Â· in-memory Â· staff workspace</span></footer>
      </div>
    </main>
    {modal && <Modal title={modal==='guest'?'register guest':modal==='maintenance'?'report maintenance issue':modal==='staff'?'hire staff':'create booking'} close={()=>setModal('')}><ActionForm type={modal} guests={guests} rooms={rooms} busy={busy} close={()=>setModal('')} submit={data => runAction(async()=>{ if(modal==='guest'){const g=await request('/guests',{method:'POST',body:JSON.stringify(data)}); localStorage.setItem('hms-customer-id', g.id); localStorage.setItem('hms-customer-name', data.name); setCustomerName(data.name); switchRole('customer'); return `guest registered Â· ${g.id}`;} if(modal==='staff'){const staff=await request('/staff',{method:'POST',body:JSON.stringify(data)});return `${staff.name} hired as ${staff.designation}`;} const staffId=role==='customer'?'S000':role==='manager'?'S002':role==='maintenance'?'S003':'S001'; if(modal==='maintenance'){await request('/maintenance',{method:'POST',body:JSON.stringify({...data,staffId})});return `room ${data.roomNumber} marked for maintenance`;} let guestId=data.guestId; if(data.newGuest){const g=await request('/guests',{method:'POST',body:JSON.stringify(data.newGuest)}); guestId=g.id; if(role==='customer') {localStorage.setItem('hms-customer-id', g.id); localStorage.setItem('hms-customer-name', data.newGuest.name); setCustomerName(data.newGuest.name);}} const b=await request('/bookings',{method:'POST',body:JSON.stringify({guestId,roomNumber:data.roomNumber,nights:data.nights,staffId})});return `booking created Â· ${b.id}`;})}/></Modal>}
    {pendingInvoice && paymentOpen && <Modal title="complete payment" close={() => setPaymentOpen(false)}><PaymentForm invoice={pendingInvoice} busy={busy} close={() => setPaymentOpen(false)} submit={payload => runAction(async () => { const result = await request(`/pay/${pendingInvoice.invoiceId}`, { method: 'POST', body: JSON.stringify(payload) }); if (!result.success) throw new Error(result.error || result.receipt || 'payment failed'); setPendingInvoice(null); setPaymentOpen(false); return `payment received: ${result.receipt}`; })}/></Modal>}
    {loading && <div className="loading"><LoaderCircle size={16}/> syncing</div>}
  </div>;
}
function Stat({icon:Icon,label,value,foot,tone=''}) { return <div className="stat-card"><div className={`stat-icon ${tone}`}><Icon size={18}/></div><div className="stat-label">{label}</div><div className="stat-value">{value}</div><div className="stat-foot">{foot}</div></div>; }
function Status({status}) { const cls = status==='AVAILABLE'?'available':status==='OCCUPIED'||status==='CONFIRMED'?'occupied':status==='UNDER_MAINTENANCE'?'maintenance':status==='COMPLETED'?'completed':'neutral'; return <span className={`status ${cls}`}><i/>{pretty(status)}</span>; }
function RoomRow({room:r}) { return <div className="room-row"><div className="room-number">{r.number}</div><div className="room-info"><strong>{r.type} room</strong><span>floor {r.floor} Â· {money(r.rate)} / night</span></div><Status status={r.status}/></div>; }
function TableHead({cols}) { return <div className="table-head">{cols.map(c=><span key={c}>{c}</span>)}</div>; }
function Empty({text}) { return <div className="empty"><div className="empty-icon"><Settings2 size={19}/></div><span>{text}</span></div>; }
function Modal({title,close,children}) { return <div className="modal-backdrop" onMouseDown={e=>{if(e.target===e.currentTarget)close();}}><div className="modal"><div className="modal-head"><div><span className="eyebrow">FRONT DESK</span><h2>{title}</h2></div><button className="icon-button" onClick={close}><X size={17}/></button></div>{children}</div></div>; }
function PaymentForm({invoice,busy,submit,close}) {
 const [mode,setMode] = useState('card');
 const [cardNumber,setCardNumber] = useState('');
 const [amountTendered,setAmountTendered] = useState(String(invoice.total));
 const [upiId,setUpiId] = useState('');
 const pay=e=>{
   e.preventDefault();
   const payload={mode};
   if(mode==='card') Object.assign(payload,{cardNumber,cardHolder:invoice.guest});
   if(mode==='cash') Object.assign(payload,{amountTendered:Number(amountTendered)});
   if(mode==='upi') Object.assign(payload,{upiId});
   submit(payload);
 };
 return <form className="action-form" onSubmit={pay}>
   <p className="form-note">Invoice {invoice.invoiceId} - total {money(invoice.total)}</p>
   <label>payment method<select value={mode} onChange={e=>setMode(e.target.value)}><option value="card">Card</option><option value="cash">Cash</option><option value="upi">UPI</option></select></label>
   {mode==='card' && <><label>demo card number<input type="text" inputMode="numeric" autoComplete="off" maxLength="16" pattern="[0-9]{16}" required value={cardNumber} onChange={e=>setCardNumber(e.target.value.replace(/\D/g,'').slice(0,16))} onKeyDown={e=>{if(e.key==='Enter')e.preventDefault();}} placeholder="enter exactly 16 digits"/></label><p className="form-note">Demo only. Do not enter real card details. Typing the number will not charge the invoice.</p></>}
   {mode==='cash' && <label>cash tendered<input type="number" min={invoice.total} step="0.01" required value={amountTendered} onChange={e=>setAmountTendered(e.target.value)}/></label>}
   {mode==='upi' && <label>UPI ID<input type="text" autoComplete="off" pattern="[A-Za-z0-9._-]+@[A-Za-z]+" required value={upiId} onChange={e=>setUpiId(e.target.value)} placeholder="name@bank"/></label>}
   <div className="form-actions"><button type="button" className="button secondary" onClick={close}>leave unpaid</button><button className="button primary" disabled={busy}>{busy?'processing...':'confirm demo payment'}</button></div>
 </form>;
}
function ActionForm({type,guests,rooms,busy,submit,close}) {
 const [form,setForm] = useState({guestId:guests[0]?.id||'',roomNumber:rooms.find(r=>r.status==='AVAILABLE')?.number||'',nights:1,name:'',phone:'',email:'',idProof:'',issue:'',id:'',designation:'Receptionist',salary:'',shift:'Morning'});
 useEffect(()=>setForm(f=>({...f,guestId:f.guestId||guests[0]?.id||'',roomNumber:f.roomNumber||rooms.find(r=>r.status==='AVAILABLE')?.number||''})),[guests,rooms]);
 const set=(k,v)=>setForm(f=>({...f,[k]:v}));
 return <form className="action-form" onSubmit={e=>{e.preventDefault(); if(type==='guest')submit({name:form.name,phone:form.phone,email:form.email,idProof:form.idProof}); else if(type==='maintenance')submit({roomNumber:Number(form.roomNumber),issue:form.issue}); else submit({guestId:form.guestId,roomNumber:Number(form.roomNumber),nights:Number(form.nights),newGuest:form.guestId==='__new__'?{name:form.name,phone:form.phone,email:form.email,idProof:form.idProof}:null});}}>
 {type==='guest' && <><label>full name<input required value={form.name} onChange={e=>set('name',e.target.value)} placeholder="guest name"/></label><label>phone<input required pattern="[0-9]{10}" inputMode="numeric" maxLength="10" value={form.phone} onChange={e=>set('phone',e.target.value)} placeholder="10-digit phone number"/></label><label>email<input required type="email" value={form.email} onChange={e=>set('email',e.target.value)} placeholder="name@example.com"/></label><label>id proof<input value={form.idProof} onChange={e=>set('idProof',e.target.value)} placeholder="optional reference"/></label></>}
 {type==='staff' && <><label>staff id<input required pattern="S[0-9]{3}" value={form.id} onChange={e=>set('id',e.target.value.toUpperCase())} placeholder="S004"/></label><label>full name<input required value={form.name} onChange={e=>set('name',e.target.value)} placeholder="employee name"/></label><label>phone<input required pattern="[0-9]{10}" inputMode="numeric" maxLength="10" value={form.phone} onChange={e=>set('phone',e.target.value)} placeholder="10-digit phone number"/></label><label>designation<select required value={form.designation} onChange={e=>set('designation',e.target.value)}><option>Receptionist</option><option>Manager</option><option>Maintenance</option><option>Housekeeping</option></select></label><label>salary<input required type="number" min="1" value={form.salary} onChange={e=>set('salary',e.target.value)} placeholder="monthly salary"/></label><label>shift<select required value={form.shift} onChange={e=>set('shift',e.target.value)}><option>Morning</option><option>Day</option><option>Evening</option><option>Night</option></select></label></>}
 {type==='maintenance' && <><label>room<select required value={form.roomNumber} onChange={e=>set('roomNumber',e.target.value)}>{rooms.filter(r=>r.status!=='OCCUPIED').map(r=><option key={r.number} value={r.number}>#{r.number} Â· {r.type} Â· {pretty(r.status)}</option>)}</select></label><label>describe the issue<textarea required minLength={3} value={form.issue} onChange={e=>set('issue',e.target.value)} placeholder="what needs fixing?"/></label></>}
 {type==='booking' && <><label>guest<select required value={form.guestId} onChange={e=>set('guestId',e.target.value)}><option value="__new__">+ use a new guest</option>{guests.map(g=><option key={g.id} value={g.id}>{g.name} Â· {g.id}</option>)}</select></label>{form.guestId==='__new__' && <><label>full name<input required value={form.name} onChange={e=>set('name',e.target.value)} placeholder="new guest name"/></label><label>phone<input required pattern="[0-9]{10}" inputMode="numeric" maxLength="10" value={form.phone} onChange={e=>set('phone',e.target.value)} placeholder="10-digit phone number"/></label><label>email<input required type="email" value={form.email} onChange={e=>set('email',e.target.value)} placeholder="name@example.com"/></label><label>id proof<input value={form.idProof} onChange={e=>set('idProof',e.target.value)} placeholder="optional reference"/></label></>}<label>available room<select required value={form.roomNumber} onChange={e=>set('roomNumber',e.target.value)}>{rooms.filter(r=>r.status==='AVAILABLE').map(r=><option key={r.number} value={r.number}>#{r.number} Â· {r.type} Â· {money(r.rate)}/night</option>)}</select></label><label>number of nights<input type="number" min="1" max="365" required value={form.nights} onChange={e=>set('nights',e.target.value)}/></label><p className="form-note">check-in starts today Â· the java backend validates availability</p></>}
 <div className="form-actions"><button type="button" className="button secondary" onClick={close}>cancel</button><button className="button primary" disabled={busy}>{busy?'savingâ€¦':type==='guest'?'register guest':type==='staff'?'hire staff':type==='maintenance'?'report issue':'confirm booking'}</button></div>
 </form>;
}

createRoot(document.getElementById('root')).render(<App/>);
