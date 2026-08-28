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
        private readonly string _writeKey;
        private readonly string _bucketSlug;

        public CosmicService(HttpClient httpClient, IConfiguration configuration)
        {
            _httpClient = httpClient;
            _bucketSlug = configuration["Cosmic:BucketSlug"] ?? string.Empty;
            _readKey = configuration["Cosmic:ReadKey"] ?? string.Empty;
            _writeKey = configuration["Cosmic:WriteKey"] ?? string.Empty;
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

        public async Task<bool> CreateArticleAsync(ArticleDto dto)
        {
            var url = $"https://api.cosmicjs.com/v3/buckets/{_bucketSlug}/objects";

            var payload = new
            {
                title = dto.Title,
                type = "articles",
                slug = !string.IsNullOrEmpty(dto.Slug) ? dto.Slug : dto.Title.ToLower().Replace(" ", "-"),
                metadata = new
                {
                    content = dto.Content,
                    summary = dto.Summary
                }
            };

            try
            {
                var request = new HttpRequestMessage(HttpMethod.Post, url)
                {
                    Content = JsonContent.Create(payload)
                };
                request.Headers.Add("Authorization", $"Bearer {_writeKey}");

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[Cosmic CREATE ERROR]: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> UpdateArticleAsync(string id, ArticleDto dto)
        {
            var url = $"https://api.cosnicjs.com/v3/buckets/{_bucketSlug}/objects/{id}"; 
            url = $"https://api.cosmicjs.com/v3/buckets/{_bucketSlug}/objects/{id}";

            var payload = new
            {
                title = dto.Title,
                slug = dto.Slug,
                metadata = new
                {
                    content = dto.Content,
                    summary = dto.Summary
                }
            };

            try
            {
                var request = new HttpRequestMessage(HttpMethod.Patch, url)
                {
                    Content = JsonContent.Create(payload)
                };
                request.Headers.Add("Authorization", $"Bearer {_writeKey}");

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[Cosmic UPDATE ERROR]: {ex.Message}");
                return false;
            }
        }

        public async Task<bool> DeleteArticleAsync(string id)
        {
            var url = $"https://api.cosmicjs.com/v3/buckets/{_bucketSlug}/objects/{id}";

            try
            {
                var request = new HttpRequestMessage(HttpMethod.Delete, url);
                request.Headers.Add("Authorization", $"Bearer {_writeKey}");

                var response = await _httpClient.SendAsync(request);
                return response.IsSuccessStatusCode;
            }
            catch (Exception ex)
            {
                Console.WriteLine($"[Cosmic DELETE ERROR]: {ex.Message}");
                return false;
            }
        }
    }
}