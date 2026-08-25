using System.Collections.Generic;
using System.Text.Json.Serialization;

namespace Interop.Common
{
    public class CosmicArticleResponse
    {
        [JsonPropertyName("objects")]
        public List<ArticleDto> Objects { get; set; } = new();
    }

    public class ArticleDto
    {
        [JsonPropertyName("id")]
        public string Id { get; set; } = string.Empty;

        [JsonPropertyName("title")]
        public string Title { get; set; } = string.Empty;

        [JsonPropertyName("slug")]
        public string Slug { get; set; } = string.Empty;

        // Podrška za ravni JSON (iz Baze)
        [JsonPropertyName("content")]
        public string DirectContent { get; set; } = string.Empty;

        [JsonPropertyName("summary")]
        public string DirectSummary { get; set; } = string.Empty;

        // Podrška za ugniježđeni JSON (s Cosmic-a)
        [JsonPropertyName("metadata")]
        public ArticleMetadata Metadata { get; set; } = new();

        // Pametna svojstva za XAML Binding koji rade u oba slučaja
        [JsonIgnore]
        public string Content
        {
            get
            {
                if (!string.IsNullOrEmpty(Metadata?.Content)) return Metadata.Content;
                return DirectContent;
            }
            set
            {
                DirectContent = value;
                if (Metadata == null) Metadata = new ArticleMetadata();
                Metadata.Content = value;
            }
        }

        [JsonIgnore]
        public string Summary
        {
            get
            {
                if (!string.IsNullOrEmpty(Metadata?.Summary)) return Metadata.Summary;
                if (!string.IsNullOrEmpty(DirectSummary)) return DirectSummary;
                return Content;
            }
            set
            {
                DirectSummary = value;
                if (Metadata == null) Metadata = new ArticleMetadata();
                Metadata.Summary = value;
            }
        }
    }

    public class ArticleMetadata
    {
        [JsonPropertyName("summary")]
        public string Summary { get; set; } = string.Empty;

        [JsonPropertyName("content")]
        public string Content { get; set; } = string.Empty;

        [JsonPropertyName("author")]
        public string Author { get; set; } = "Admin";

        [JsonPropertyName("category")]
        public string Category { get; set; } = "Opće";

        [JsonPropertyName("order")]
        public int? Order { get; set; }
    }
}