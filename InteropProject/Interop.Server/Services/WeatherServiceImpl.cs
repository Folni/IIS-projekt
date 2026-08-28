using System.Xml.Linq;
using Grpc.Core;
using Interop.Server.Protos;

namespace Interop.Server.Services
{
    public class WeatherServiceImpl : WeatherService.WeatherServiceBase
    {
        private readonly HttpClient _httpClient;
        private const string DhmzUrl = "https://vrijeme.hr/hrvatska_n.xml";

        public WeatherServiceImpl(HttpClient httpClient)
        {
            _httpClient = httpClient;
        }

        public override async Task<WeatherResponse> GetTemperature(WeatherRequest request, ServerCallContext context)
        {
            var response = new WeatherResponse();
            var searchTerm = (request.CityName ?? string.Empty).Trim().ToLower();

            try
            {
                var xmlStream = await _httpClient.GetStreamAsync(DhmzUrl);
                var doc = XDocument.Load(xmlStream);

                var cityNodes = doc.Descendants("Grad");

                foreach (var city in cityNodes)
                {
                    var cityName = city.Element("GradIme")?.Value ?? string.Empty;
                    var temp = city.Element("Podatci")?.Element("Temp")?.Value ?? "N/A";

                    if (string.IsNullOrWhiteSpace(searchTerm) || cityName.ToLower().Contains(searchTerm))
                    {
                        response.Cities.Add(new CityTemperature
                        {
                            CityName = cityName,
                            Temperature = temp
                        });
                    }
                }
            }
            catch (Exception ex)
            {
                throw new RpcException(new Status(StatusCode.Internal, $"Greška pri dohvat DHMZ podataka: {ex.Message}"));
            }

            return response;
        }
    }
}