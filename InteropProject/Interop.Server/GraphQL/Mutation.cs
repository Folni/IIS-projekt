using HotChocolate;
using Interop.Server.Data;

namespace Interop.Server.GraphQL
{
    public class Mutation
    {
        public async Task<ArticleEntity> AddArticleAsync([Service] AppDbContext context, string title, string summary, string content)
        {
            var article = new ArticleEntity
            {
                Id = Guid.NewGuid().ToString(),
                Title = title,
                Summary = summary,
                Content = content,
                Slug = title.ToLower().Replace(" ", "-")
            };

            context.Articles.Add(article);
            await context.SaveChangesAsync();
            return article;
        }
    }
}