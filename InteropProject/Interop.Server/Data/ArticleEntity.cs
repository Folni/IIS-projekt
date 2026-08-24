using System.ComponentModel.DataAnnotations;

namespace Interop.Server.Data
{
    public class ArticleEntity
    {
        [Key]
        public string Id { get; set; } = Guid.NewGuid().ToString();

        [Required]
        public string Title { get; set; } = string.Empty;

        public string Slug { get; set; } = string.Empty;
        public string Summary { get; set; } = string.Empty;
        public string Content { get; set; } = string.Empty;
        public int? Order { get; set; }
        public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    }
}