const Input = ({ label, error, className = "", ...props }) => {
  return (
    <div className="w-full">
      {label && <label className="mb-1 block text-sm font-medium text-slate-200">{label}</label>}
      <input
        className={`w-full rounded-md border border-slate-700 bg-slate-950/60 px-3 py-2 text-sm text-slate-100 outline-none transition focus:border-blue-500 focus:ring-2 focus:ring-blue-400/20 ${className}`}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
};

export default Input;
