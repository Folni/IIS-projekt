using HotChocolate;
using Interop.Server.Data;

namespace Interop.Server.GraphQL
{
    public class Query
    {
        public IQueryable<ArticleEntity> GetArticles([Service] AppDbContext context)
        {
            return context.Articles;
        }
    }
}