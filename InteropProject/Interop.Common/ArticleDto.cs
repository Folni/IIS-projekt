using System;
using System.Collections.Generic;
using System.Text;
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

        [JsonPropertyName("metadata")]
        public ArticleMetadata Metadata { get; set; } = new();
    }

    public class ArticleMetadata
    {
        [JsonPropertyName("summary")]
        public string Summary { get; set; } = string.Empty;

        [JsonPropertyName("content")]
        public string Content { get; set; } = string.Empty;

        [JsonPropertyName("order")]
        public int? Order { get; set; }
    }
}