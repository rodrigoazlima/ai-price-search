HelloWorldController - cURL example

GET /hello

Description: Returns a simple greeting message.

cURL Example:

# Default (Linux/macOS, Git Bash, WSL)
curl -i http://localhost:8080/hello

# PowerShell (Windows)
# Note: curl is an alias for Invoke-WebRequest in PowerShell; use "curl.exe" for the native cURL.
curl.exe -i http://localhost:8080/hello

Expected Response:
HTTP/1.1 200 OK
Content-Type: text/plain;charset=UTF-8

Hello, World!
