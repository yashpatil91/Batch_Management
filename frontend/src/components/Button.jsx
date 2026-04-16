const variants = {
  primary: "bg-blue-600 text-white hover:bg-blue-700",
  secondary: "bg-slate-800 text-slate-100 hover:bg-slate-700",
  danger: "bg-red-600 text-white hover:bg-red-700"
};

const Button = ({ type = "button", variant = "primary", className = "", loading = false, children, ...props }) => {
  return (
    <button
      type={type}
      disabled={loading || props.disabled}
      className={`rounded-md px-4 py-2 text-sm font-medium transition disabled:cursor-not-allowed disabled:opacity-60 ${variants[variant]} ${className}`}
      {...props}
    >
      {loading ? "Please wait..." : children}
    </button>
  );
};

export default Button;
