"use client"; // This marks the file as a Client Component

import { useState, FormEvent, ChangeEvent } from "react";
import axios from "axios";

// Define the types for the API response
interface Result {
  score: number;
  severity: string;
  vectorString: string;
}

// Define a type for Axios error response
interface AxiosError {
  response?: {
    data?: {
      message?: string;
    };
  };
}

export default function Home() {
  const [vector, setVector] = useState<string>("");
  const [file, setFile] = useState<File | null>(null);
  const [results, setResults] = useState<Result | null>(null); // Use the Result interface
  const [error, setError] = useState<string>("");

  const handleFileChange = (e: ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      setFile(e.target.files[0]);
    } else {
      setFile(null);
    }
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError("");
    setResults(null);

    if (!vector && !file) {
      setError("Please provide a vector string or upload a file.");
      return;
    }

    try {
      let data: Result;
      if (file) {
        const formData = new FormData();
        formData.append("file", file);
      //  const response = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/upload`, { vector });

        const response = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/upload`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        data = response.data;
      } else {
        const response = await axios.post(`${process.env.NEXT_PUBLIC_API_URL}/api/calculate`, { vector });

       // const response = await axios.post("`${process.env.NEXT_PUBLIC_API_URL}/api/calculate`", { vector });
        data = response.data;
      }
      setResults(data);

      // Clear the input fields
      setVector("");
      setFile(null);
    } catch (err) {
      const axiosError = err as AxiosError;
      setError(axiosError.response?.data?.message || "Error processing your request.");
    }
  };

  return (
    <div className="max-w-2xl mx-auto p-6 bg-white shadow-md rounded-lg">
      <h1 className="text-3xl font-bold text-center text-gray-800 mb-6">CVSS Calculator</h1>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-lg font-medium text-gray-700">Vector String:</label>
          <input
            type="text"
            value={vector}
            onChange={(e) => setVector(e.target.value)}
            placeholder="Enter vector string"
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white text-black"
          />
        </div>
        <div>
          <label className="block text-lg font-medium text-gray-700">Upload File:</label>
          <input
            type="file"
            accept=".txt"
            onChange={handleFileChange}
            className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white text-black"
          />
        </div>
        <div>
          <button
            type="submit"
            className="w-full py-3 px-4 bg-blue-600 text-white font-semibold rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Calculate
          </button>
        </div>
      </form>
      {error && <p className="text-red-500 mt-4 text-center">{error}</p>}
      {results && (
        <pre className="bg-white text-black p-4 rounded-lg mt-4 whitespace-pre-wrap break-words">{JSON.stringify(results, null, 2)}</pre>
      )}
    </div>
  );
}
