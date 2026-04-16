export const validateEmail = (email) => {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
};

export const validateLoginForm = ({ email, password }) => {
  const errors = {};
  if (!email?.trim()) errors.email = "Email is required";
  else if (!validateEmail(email)) errors.email = "Enter a valid email";
  if (!password?.trim()) errors.password = "Password is required";
  return errors;
};

export const validateTrainerForm = ({ name, email, password }) => {
  const errors = {};
  if (!name?.trim()) errors.name = "Name is required";
  if (!email?.trim()) errors.email = "Email is required";
  else if (!validateEmail(email)) errors.email = "Enter a valid email";
  if (!password?.trim()) errors.password = "Password is required";
  else if (password.trim().length < 6) errors.password = "Password must be at least 6 characters";
  return errors;
};
