using Microsoft.EntityFrameworkCore;

namespace Interop.Server.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<ArticleEntity> Articles => Set<ArticleEntity>();
    }
}