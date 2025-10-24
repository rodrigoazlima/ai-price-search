Actuator Endpoints - cURL examples

GET /actuator/health

Description: Checks the application health. Details are enabled (show-details: always).

cURL Example:

# Default (Linux/macOS, Git Bash, WSL)
curl -i http://localhost:8080/actuator/health

# PowerShell (Windows)
# Note: curl is an alias for Invoke-WebRequest in PowerShell; use "curl.exe" for the native cURL.
curl.exe -i http://localhost:8080/actuator/health

Expected Response (example):
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" }
  }
}

---

GET /actuator/info

Description: Returns arbitrary application info if configured. Default may be empty object.

cURL Example:

# Default (Linux/macOS, Git Bash, WSL)
curl -i http://localhost:8080/actuator/info

# PowerShell (Windows)
# Note: curl is an alias for Invoke-WebRequest in PowerShell; use "curl.exe" for the native cURL.
curl.exe -i http://localhost:8080/actuator/info

Expected Response (example):
HTTP/1.1 200 OK
Content-Type: application/json

{}
