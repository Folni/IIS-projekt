using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Interop.Common;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;

namespace Interop.Server.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AuthController : ControllerBase
    {
        private readonly IConfiguration _config;
        private static readonly Dictionary<string, (string Username, string Role)> RefreshTokens = new();

        public AuthController(IConfiguration config)
        {
            _config = config;
        }

        [HttpPost("login")]
        public IActionResult Login([FromBody] LoginModel model)
        {
            string role = string.Empty;
            if (model.Username == "admin" && model.Password == "admin123")
                role = "FullAccess";
            else if (model.Username == "user" && model.Password == "user123")
                role = "ReadOnly";
            else
                return Unauthorized("Neispravno korisničko ime ili lozinka.");

            var accessToken = GenerateAccessToken(model.Username, role);
            var refreshToken = Guid.NewGuid().ToString();

            RefreshTokens[refreshToken] = (model.Username, role);

            return Ok(new TokenResponseDto
            {
                AccessToken = accessToken,
                RefreshToken = refreshToken,
                Role = role
            });
        }

        [HttpPost("refresh")]
        public IActionResult Refresh([FromBody] RefreshTokenRequestDto request)
        {
            if (RefreshTokens.TryGetValue(request.RefreshToken, out var userDetails))
            {
                var newAccessToken = GenerateAccessToken(userDetails.Username, userDetails.Role);
                var newRefreshToken = Guid.NewGuid().ToString();

                RefreshTokens.Remove(request.RefreshToken);
                RefreshTokens[newRefreshToken] = userDetails;

                return Ok(new TokenResponseDto
                {
                    AccessToken = newAccessToken,
                    RefreshToken = newRefreshToken,
                    Role = userDetails.Role
                });
            }

            return BadRequest("Nevaljan Refresh Token.");
        }

        private string GenerateAccessToken(string username, string role)
        {
            var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_config["Jwt:Key"]!));
            var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

            var claims = new[]
            {
                new Claim(ClaimTypes.Name, username),
                new Claim(ClaimTypes.Role, role)
            };

            var token = new JwtSecurityToken(
                issuer: _config["Jwt:Issuer"],
                audience: _config["Jwt:Audience"],
                claims: claims,
                expires: DateTime.UtcNow.AddMinutes(15),
                signingCredentials: creds
            );

            return new JwtSecurityTokenHandler().WriteToken(token);
        }
    }
}