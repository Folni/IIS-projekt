using System.Xml;
using System.Xml.Linq;
using Interop.Common;

namespace Interop.Server.Services
{
    public class SoapService : ISoapService
    {
        private readonly CosmicService _cosmicService;
        private readonly IWebHostEnvironment _env;

        public SoapService(CosmicService cosmicService, IWebHostEnvironment env)
        {
            _cosmicService = cosmicService;
            _env = env;
        }

        public async Task<List<ArticleDto>> SearchArticlesXml(string searchTerm)
        {
            var articles = await _cosmicService.GetArticlesAsync();

            var xmlPath = System.IO.Path.Combine(_env.ContentRootPath, "articles_cosmic.xml");
            var xDoc = new XDocument(
                new XElement("Articles",
                    articles.Select(a => new XElement("Article",
                        new XElement("Title", a.Title),
                        new XElement("Summary", a.Metadata.Summary),
                        new XElement("Content", a.Metadata.Content),
                        new XElement("Order", a.Metadata.Order.HasValue ? a.Metadata.Order.Value.ToString() : "")
                    ))
                )
            );
            xDoc.Save(xmlPath);

            var filteredArticles = new List<ArticleDto>();
            XmlDocument doc = new XmlDocument();
            doc.Load(xmlPath);

            string cleanSearch = searchTerm.ToLower();
            string xpathQuery = $"//Article[contains(translate(Title, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '{cleanSearch}') or contains(translate(Summary, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '{cleanSearch}') or contains(translate(Content, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '{cleanSearch}')]";

            XmlNodeList? nodes = doc.SelectNodes(xpathQuery);
            if (nodes != null)
            {
                foreach (XmlNode node in nodes)
                {
                    filteredArticles.Add(new ArticleDto
                    {
                        Title = node["Title"]?.InnerText ?? string.Empty,
                        Metadata = new ArticleMetadata
                        {
                            Summary = node["Summary"]?.InnerText ?? string.Empty,
                            Content = node["Content"]?.InnerText ?? string.Empty,
                            Order = int.TryParse(node["Order"]?.InnerText, out int ord) ? ord : null
                        }
                    });
                }
            }

            return filteredArticles;
        }
    }
}