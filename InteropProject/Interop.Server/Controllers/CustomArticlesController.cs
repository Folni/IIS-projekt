using Interop.Server.Data;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Interop.Server.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize] // Zahtijeva JWT Token
    public class CustomArticlesController : ControllerBase
    {
        private readonly AppDbContext _context;

        public CustomArticlesController(AppDbContext context)
        {
            _context = context;
        }

        // GET: api/CustomArticles
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ArticleEntity>>> GetArticles()
        {
            return await _context.Articles.ToListAsync();
        }

        // GET: api/CustomArticles/5
        [HttpGet("{id}")]
        public async Task<ActionResult<ArticleEntity>> GetArticle(string id)
        {
            var article = await _context.Articles.FindAsync(id);
            if (article == null) return NotFound();
            return article;
        }

        // POST: api/CustomArticles (Samo FullAccess uloga)
        [HttpPost]
        [Authorize(Roles = "FullAccess")]
        public async Task<ActionResult<ArticleEntity>> CreateArticle(ArticleEntity article)
        {
            article.Id = Guid.NewGuid().ToString();
            _context.Articles.Add(article);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetArticle), new { id = article.Id }, article);
        }

        // PUT: api/CustomArticles/5 (Samo FullAccess uloga)
        [HttpPut("{id}")]
        [Authorize(Roles = "FullAccess")]
        public async Task<IActionResult> UpdateArticle(string id, ArticleEntity article)
        {
            if (id != article.Id) return BadRequest();

            _context.Entry(article).State = EntityState.Modified;
            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.Articles.Any(e => e.Id == id)) return NotFound();
                throw;
            }

            return NoContent();
        }

        // DELETE: api/CustomArticles/5 (Samo FullAccess uloga)
        [HttpDelete("{id}")]
        [Authorize(Roles = "FullAccess")]
        public async Task<IActionResult> DeleteArticle(string id)
        {
            var article = await _context.Articles.FindAsync(id);
            if (article == null) return NotFound();

            _context.Articles.Remove(article);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }
}