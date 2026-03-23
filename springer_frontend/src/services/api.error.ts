import axios from "axios";

export interface ApiErrorResponse<T = unknown> {
  success: boolean;
  message: string;
  data?: T;
}

export interface AppError<T = unknown> {
  message: string;
  success: boolean;
  data?: T;
}

export function handleAxiosError<T = unknown>(error: unknown): AppError<T> {

  // CASE 1: Axios error
  if (axios.isAxiosError(error)) {
    const apiError = error.response?.data as ApiErrorResponse<T> | undefined;

    if (apiError) {
      return {
        message: apiError.message || "Server error",
        success: false,
        data: apiError.data // 🔥 PRESERVE DATA
      };
    }

    return {
      message: error.message || "Unknown server error",
      success: false
    };
  }

  // CASE 2: Manually thrown AppError
  if (typeof error === "object" && error !== null && "message" in error) {
    const err = error as AppError<T>;
    return {
      message: err.message,
      success: false,
      data: err.data // 🔥 PRESERVE IF EXISTS
    };
  }

  // CASE 3: Unexpected error
  return {
    message: "Unexpected error occurred",
    success: false
  };
}