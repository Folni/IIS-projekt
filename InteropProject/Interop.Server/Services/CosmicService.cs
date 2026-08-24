using System.Net.Http.Json;
using System.Text.Json;
using Interop.Common;
using Microsoft.Extensions.Configuration;

namespace Interop.Server.Services
{
    public class CosmicService
    {
        private readonly HttpClient _httpClient;
        private readonly string _readKey;
        private readonly string _bucketSlug;

        public CosmicService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;
            _bucketSlug = configuration["Cosmic:BucketSlug"] ?? string.Empty;
            _readKey = configuration["Cosmic:ReadKey"] ?? string.Empty;
        }

        public async Task<List<ArticleDto>> GetArticlesAsync()
        {
            var url = $"https://api.cosmicjs.com/v3/buckets/{_bucketSlug}/objects?type=articles&read_key={_readKey}";

            try
            {
                var options = new JsonSerializerOptions
                {
                    PropertyNameCaseInsensitive = true
                };

                var response = await _httpClient.GetFromJsonAsync<CosmicArticleResponse>(url, options);
                return response?.Objects ?? new List<ArticleDto>();
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[Cosmic ERROR]: {ex.Message}");
                return new List<ArticleDto>();
            }
        }
    }
}