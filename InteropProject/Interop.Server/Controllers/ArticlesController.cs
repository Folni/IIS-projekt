using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.Json;
using System.Threading.Tasks;
using System.Xml.Linq;
using Interop.Common;
using Interop.Server.Data;
using Interop.Server.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Interop.Server.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ArticlesController : ControllerBase
    {
        private readonly CosmicService _cosmicService;
        private readonly ValidationService _validationService;
        private readonly AppDbContext _context;

        public ArticlesController(CosmicService cosmicService, ValidationService validationService, AppDbContext context)
        {
            _cosmicService = cosmicService;
            _validationService = validationService;
            _context = context;
        }


        [HttpGet("cosmic")]
        public async Task<ActionResult<List<ArticleDto>>> GetCosmicArticles()
        {
            var articles = await _cosmicService.GetArticlesAsync();
            return Ok(articles);
        }

        [HttpGet("db")]
        public async Task<ActionResult<List<ArticleEntity>>> GetDbArticles()
        {
            var articles = await _context.Articles.ToListAsync();
            return Ok(articles);
        }


        [HttpPost("cosmic")]
        [Authorize(Roles = "Admin,FullAccess")]
        public async Task<IActionResult> CreateCosmicArticle([FromBody] ArticleDto dto)
        {
            try
            {
                bool success = await _cosmicService.CreateArticleAsync(dto);
                if (success)
                {
                    return Ok(new { Message = "Članak uspješno dodan na Cosmic CMS!" });
                }
                return BadRequest(new { Message = "Greška pri dodavanju članka na Cosmic CMS." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { Message = "Serverska greška pri radu s Cosmic API-jem", Error = ex.Message });
            }
        }

        [HttpPut("cosmic/{id}")]
        [Authorize(Roles = "Admin,FullAccess")]
        public async Task<IActionResult> UpdateCosmicArticle(string id, [FromBody] ArticleDto dto)
        {
            try
            {
                bool success = await _cosmicService.UpdateArticleAsync(id, dto);
                if (success)
                {
                    return Ok(new { Message = "Članak uspješno ažuriran na Cosmic CMS-u!" });
                }
                return BadRequest(new { Message = "Greška pri ažuriranju članka na Cosmic CMS-u." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { Message = "Serverska greška pri radu s Cosmic API-jem", Error = ex.Message });
            }
        }

        [HttpDelete("cosmic/{id}")]
        [Authorize(Roles = "Admin,FullAccess")]
        public async Task<IActionResult> DeleteCosmicArticle(string id)
        {
            try
            {
                bool success = await _cosmicService.DeleteArticleAsync(id);
                if (success)
                {
                    return Ok(new { Message = "Članak uspješno obrisan s Cosmic CMS-a!" });
                }
                return BadRequest(new { Message = "Greška pri brisanju članka s Cosmic CMS-a." });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new { Message = "Serverska greška pri radu s Cosmic API-jem", Error = ex.Message });
            }
        }


        [HttpPost("upload-xml")]
        [Authorize(Roles = "Admin,FullAccess")]
        public async Task<IActionResult> UploadXml([FromBody] string xmlContent)
        {
            var errors = _validationService.ValidateXml(xmlContent);
            if (errors.Any())
            {
                return BadRequest(new { Message = "XML validacija nije prošla!", Errors = errors });
            }

            try
            {
                XDocument doc = XDocument.Parse(xmlContent);
                XElement root = doc.Root!;

                var entity = new ArticleEntity
                {
                    Title = root.Element("Title")?.Value ?? string.Empty,
                    Summary = root.Element("Summary")?.Value ?? string.Empty,
                    Content = root.Element("Content")?.Value ?? string.Empty,
                    Order = int.TryParse(root.Element("Order")?.Value, out int orderVal) ? orderVal : null,
                    Slug = (root.Element("Title")?.Value ?? string.Empty).ToLower().Replace(" ", "-")
                };

                _context.Articles.Add(entity);
                await _context.SaveChangesAsync();

                return Ok(new { Message = "XML uspješno validiran i spremljen u bazu!", Entity = entity });
            }
            catch (Exception ex)
            {
                return BadRequest(new { Message = "Greška pri obradi XML-a", Error = ex.Message });
            }
        }

        [HttpPost("upload-json")]
        [Authorize(Roles = "Admin,FullAccess")]
        public async Task<IActionResult> UploadJson([FromBody] JsonElement jsonElement)
        {
            string rawJson = jsonElement.GetRawText();
            var errors = await _validationService.ValidateJsonAsync(rawJson);
            if (errors.Any())
            {
                return BadRequest(new { Message = "JSON validacija nije prošla!", Errors = errors });
            }

            try
            {
                using var doc = JsonDocument.Parse(rawJson);
                var root = doc.RootElement;

                var entity = new ArticleEntity
                {
                    Title = root.GetProperty("title").GetString() ?? string.Empty,
                    Summary = root.TryGetProperty("summary", out var s) ? s.GetString() ?? "" : "",
                    Content = root.GetProperty("content").GetString() ?? string.Empty,
                    Order = root.TryGetProperty("order", out var o) && o.ValueKind == JsonValueKind.Number ? o.GetInt32() : null,
                    Slug = (root.GetProperty("title").GetString() ?? string.Empty).ToLower().Replace(" ", "-")
                };

                _context.Articles.Add(entity);
                await _context.SaveChangesAsync();

                return Ok(new { Message = "JSON uspješno validiran i spremljen u bazu!", Entity = entity });
            }
            catch (Exception ex)
            {
                return BadRequest(new { Message = "Greška pri obradi JSON-a", Error = ex.Message });
            }
        }
    }
}