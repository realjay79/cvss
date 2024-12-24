import { useState } from "react";
import axios from "axios";

export default function Home() {
  const [vector, setVector] = useState("");
  const [file, setFile] = useState(null);
  const [results, setResults] = useState("");
  const [error, setError] = useState("");

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setResults("");

    if (!vector && !file) {
      setError("Please provide a vector string or upload a file.");
      return;
    }

    try {
      let data;
      if (file) {
        const formData = new FormData();
        formData.append("file", file);
        data = await axios.post("/api/upload", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
      } else {
        data = await axios.post("/api/calculate", { vector });
      }
      setResults(data.data);
    } catch (err) {
      setError(err.response?.data?.message || "Error processing your request.");
    }
  };

  return (
    <div>
      <h1>CVSS Calculator</h1>
      <form onSubmit={handleSubmit}>
        <label>Vector String:</label>
        <input
          type="text"
          value={vector}
          onChange={(e) => setVector(e.target.value)}
          placeholder="Enter vector string"
        />
        <br />
        <label>Upload File:</label>
        <input
          type="file"
          accept=".txt"
          onChange={(e) => setFile(e.target.files[0])}
        />
        <br />
        <button type="submit">Calculate</button>
      </form>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {results && <pre>{JSON.stringify(results, null, 2)}</pre>}
    </div>
  );
}
