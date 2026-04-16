const Card = ({ title, children, className = "" }) => {
  return (
    <div className={`rounded-xl border border-slate-800 bg-slate-900/70 p-5 shadow-lg shadow-slate-950/30 ${className}`}>
      {title && <h3 className="mb-3 text-lg font-semibold text-slate-100">{title}</h3>}
      {children}
    </div>
  );
};

export default Card;
