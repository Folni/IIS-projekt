using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Xml.Linq;
using Grpc.Net.Client;
using Interop.Common;
using Interop.Server.Protos;

namespace Interop.Client
{
    public partial class MainWindow : Window
    {
        private readonly HttpClient _httpClient;
        private const string BaseServerUrl = "https://localhost:7059";
        private string _accessToken = string.Empty;
        private string _userRole = string.Empty;

        public MainWindow()
        {
            InitializeComponent();

            var handler = new HttpClientHandler
            {
                ServerCertificateCustomValidationCallback = HttpClientHandler.DangerousAcceptAnyServerCertificateValidator
            };
            _httpClient = new HttpClient(handler) { BaseAddress = new Uri(BaseServerUrl) };

            InicijalizirajDefaultTekstove();
            PostaviDozvole(false);
        }

        private void InicijalizirajDefaultTekstove()
        {
            TxtJsonInput.Text = "{\n  \"title\": \"Novi članak iz WPF klijenta\",\n  \"summary\": \"Sažetak iz WPF-a\",\n  \"content\": \"Detaljan sadržaj unesen kroz klijentsku aplikaciju.\",\n  \"order\": 5\n}";
            TxtXmlInput.Text = "<Article>\n  <Title>WPF XML Članak</Title>\n  <Summary>Sažetak iz WPF-a</Summary>\n  <Content>Sadržaj unesen iz klijenta</Content>\n  <Order>10</Order>\n</Article>";
        }

        private void PostaviDozvole(bool canWrite)
        {
            BtnCrudCreate.IsEnabled = canWrite;
            BtnCrudUpdate.IsEnabled = canWrite;
            BtnCrudDelete.IsEnabled = canWrite;
            BtnSendJson.IsEnabled = canWrite;
            BtnSendXml.IsEnabled = canWrite;
        }

        // 1. JWT Prijava i upravljanje ulogama
        private async void BtnLogin_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var loginModel = new LoginModel
                {
                    Username = TxtUsername.Text,
                    Password = TxtPassword.Password
                };

                var response = await _httpClient.PostAsJsonAsync("/api/Auth/login", loginModel);
                if (response.IsSuccessStatusCode)
                {
                    var result = await response.Content.ReadFromJsonAsync<TokenResponseDto>();
                    if (result != null)
                    {
                        _accessToken = result.AccessToken;
                        _userRole = result.Role;

                        _httpClient.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue("Bearer", _accessToken);

                        TxtRoleStatus.Text = $"Status: Prijavljen ({_userRole})";

                        bool canWrite = _userRole == "FullAccess" || _userRole == "Admin";
                        TxtRoleStatus.Foreground = canWrite ? System.Windows.Media.Brushes.Green : System.Windows.Media.Brushes.Orange;

                        PostaviDozvole(canWrite);

                        MessageBox.Show($"Uspješna prijava! Uloga: {_userRole}\nPravo pisanja: {(canWrite ? "DA" : "NE (ReadOnly)")}", "Prijava", MessageBoxButton.OK, MessageBoxImage.Information);
                    }
                }
                else
                {
                    MessageBox.Show("Neispravno korisničko ime ili lozinka.", "Greška prijave", MessageBoxButton.OK, MessageBoxImage.Warning);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri spajanju na poslužitelj: {ex.Message}", "Greška", MessageBoxButton.OK, MessageBoxImage.Error);
            }
        }

        // 2. Cosmic API dohvat
        private async void BtnGetCosmic_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var articles = await _httpClient.GetFromJsonAsync<List<ArticleDto>>("/api/Articles/cosmic");
                DgArticles.ItemsSource = articles;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri dohvatu s Cosmic-a: {ex.Message}");
            }
        }

        // 3. Baza podataka dohvat preko CustomArticles
        private async void BtnGetDb_Click(object sender, RoutedEventArgs e)
        {
            await UcitajIzBazeAsync();
        }

        private async System.Threading.Tasks.Task UcitajIzBazeAsync()
        {
            try
            {
                var articles = await _httpClient.GetFromJsonAsync<List<ArticleDto>>("/api/CustomArticles");
                DgArticles.ItemsSource = articles;
                DgCrud.ItemsSource = articles;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri dohvatu iz baze: {ex.Message}");
            }
        }

        // 4. CRUD: Selekcija iz tablice
        private void DgCrud_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (DgCrud.SelectedItem is ArticleDto selected)
            {
                TxtCrudTitle.Text = selected.Title;
                TxtCrudSlug.Text = selected.Slug;
                TxtCrudContent.Text = selected.Content;
            }
        }

        // CRUD: Create
        private async void BtnCrudCreate_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var newArticle = new ArticleDto
                {
                    Title = TxtCrudTitle.Text,
                    Slug = TxtCrudSlug.Text,
                    Content = TxtCrudContent.Text,
                    Summary = TxtCrudContent.Text
                };

                var response = await _httpClient.PostAsJsonAsync("/api/CustomArticles", newArticle);
                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Članak uspješno dodan u bazu!");
                    await UcitajIzBazeAsync();
                }
                else
                {
                    MessageBox.Show($"Greška pri dodavanju artikla. Status: {response.StatusCode}");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

        // CRUD: Update
        private async void BtnCrudUpdate_Click(object sender, RoutedEventArgs e)
        {
            if (DgCrud.SelectedItem is not ArticleDto selected)
            {
                MessageBox.Show("Molimo odaberite članak iz tablice za izmjenu.");
                return;
            }

            try
            {
                selected.Title = TxtCrudTitle.Text;
                selected.Slug = TxtCrudSlug.Text;
                selected.Content = TxtCrudContent.Text;
                selected.Summary = TxtCrudContent.Text;

                var response = await _httpClient.PutAsJsonAsync($"/api/CustomArticles/{selected.Id}", selected);
                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Članak uspješno ažuriran!");
                    await UcitajIzBazeAsync();
                }
                else
                {
                    MessageBox.Show($"Greška pri ažuriranju artikla. Status: {response.StatusCode}");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

        // CRUD: Delete
        private async void BtnCrudDelete_Click(object sender, RoutedEventArgs e)
        {
            if (DgCrud.SelectedItem is not ArticleDto selected)
            {
                MessageBox.Show("Molimo odaberite članak iz tablice za brisanje.");
                return;
            }

            try
            {
                var response = await _httpClient.DeleteAsync($"/api/CustomArticles/{selected.Id}");
                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Članak obrisan!");
                    await UcitajIzBazeAsync();
                }
                else
                {
                    MessageBox.Show($"Greška pri brisanju artikla. Status: {response.StatusCode}");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

        // 5. Slanje JSON-a
        private async void BtnSendJson_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var content = new StringContent(TxtJsonInput.Text, Encoding.UTF8, "application/json");
                var response = await _httpClient.PostAsync("/api/Articles/upload-json", content);
                var responseText = await response.Content.ReadAsStringAsync();

                MessageBox.Show(responseText, response.IsSuccessStatusCode ? "Uspjeh" : "Validacijska Greška");
                if (response.IsSuccessStatusCode) await UcitajIzBazeAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

        // 6. Slanje XML-a
        private async void BtnSendXml_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var content = new StringContent($"\"{TxtXmlInput.Text.Replace("\"", "\\\"").Replace("\n", "").Replace("\r", "")}\"", Encoding.UTF8, "application/json");
                var response = await _httpClient.PostAsync("/api/Articles/upload-xml", content);
                var responseText = await response.Content.ReadAsStringAsync();

                MessageBox.Show(responseText, response.IsSuccessStatusCode ? "Uspjeh" : "Validacijska Greška");
                if (response.IsSuccessStatusCode) await UcitajIzBazeAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

        // 7. SOAP Pretraga (XPath / LINQ to XML)
        private async void BtnSoapSearch_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                string searchTerm = TxtSoapSearch.Text ?? string.Empty;
                string soapEnvelope = $@"<soapenv:Envelope xmlns:soapenv=""http://schemas.xmlsoap.org/soap/envelope/"" xmlns:temp=""http://tempuri.org/"">
                   <soapenv:Header/>
                   <soapenv:Body>
                      <temp:SearchArticlesXml>
                         <temp:searchTerm>{searchTerm}</temp:searchTerm>
                      </temp:SearchArticlesXml>
                   </soapenv:Body>
                </soapenv:Envelope>";

                var request = new HttpRequestMessage(HttpMethod.Post, "/Service.asmx")
                {
                    Content = new StringContent(soapEnvelope, Encoding.UTF8, "text/xml")
                };
                request.Headers.Add("SOAPAction", "http://tempuri.org/ISoapService/SearchArticlesXml");

                var response = await _httpClient.SendAsync(request);
                string xmlResult = await response.Content.ReadAsStringAsync();

                XDocument xDoc = XDocument.Parse(xmlResult);
                var articles = new List<ArticleDto>();

                foreach (var el in xDoc.Descendants().Where(x => x.Name.LocalName == "ArticleDto"))
                {
                    var meta = el.Elements().FirstOrDefault(x => x.Name.LocalName == "Metadata");

                    articles.Add(new ArticleDto
                    {
                        Id = el.Elements().FirstOrDefault(x => x.Name.LocalName == "Id")?.Value ?? string.Empty,
                        Title = el.Elements().FirstOrDefault(x => x.Name.LocalName == "Title")?.Value ?? string.Empty,
                        Slug = el.Elements().FirstOrDefault(x => x.Name.LocalName == "Slug")?.Value ?? string.Empty,
                        Content = meta?.Elements().FirstOrDefault(x => x.Name.LocalName == "Content")?.Value ?? string.Empty,
                        Summary = meta?.Elements().FirstOrDefault(x => x.Name.LocalName == "Summary")?.Value ?? string.Empty,
                        Metadata = new ArticleMetadata
                        {
                            Summary = meta?.Elements().FirstOrDefault(x => x.Name.LocalName == "Summary")?.Value ?? string.Empty,
                            Content = meta?.Elements().FirstOrDefault(x => x.Name.LocalName == "Content")?.Value ?? string.Empty,
                            Author = meta?.Elements().FirstOrDefault(x => x.Name.LocalName == "Author")?.Value ?? "Admin",
                            Category = meta?.Elements().FirstOrDefault(x => x.Name.LocalName == "Category")?.Value ?? "Opće"
                        }
                    });
                }

                DgSoapResults.ItemsSource = articles;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri SOAP pozivu: {ex.Message}");
            }
        }

        // 8. gRPC DHMZ Vremenska prognoza
        private async void BtnGrpc_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var channelHandler = new HttpClientHandler
                {
                    ServerCertificateCustomValidationCallback = HttpClientHandler.DangerousAcceptAnyServerCertificateValidator
                };

                using var channel = GrpcChannel.ForAddress(BaseServerUrl, new GrpcChannelOptions { HttpHandler = channelHandler });
                var client = new WeatherService.WeatherServiceClient(channel);

                var reply = await client.GetTemperatureAsync(new WeatherRequest { CityName = TxtGrpcCity.Text });
                DgGrpcResults.ItemsSource = reply.Cities;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri gRPC pozivu: {ex.Message}");
            }
        }
    }
}