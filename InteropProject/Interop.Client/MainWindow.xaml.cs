using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Net.Http.Json;
using System.Text;
using System.Threading.Tasks;
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

                        await UcitajPodatkeOvisnoOIzvoruAsync();
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

        private async void Source_Checked(object sender, RoutedEventArgs e)
        {
            await UcitajPodatkeOvisnoOIzvoruAsync();
        }

        private async void BtnRefresh_Click(object sender, RoutedEventArgs e)
        {
            await UcitajPodatkeOvisnoOIzvoruAsync();
        }

        private async Task UcitajPodatkeOvisnoOIzvoruAsync()
        {
            if (_httpClient == null) return;

            try
            {
                List<ArticleDto>? articles = null;

                if (RbCosmic != null && RbCosmic.IsChecked == true)
                {
                    articles = await _httpClient.GetFromJsonAsync<List<ArticleDto>>("/api/Articles/cosmic");
                }
                else
                {
                    articles = await _httpClient.GetFromJsonAsync<List<ArticleDto>>("/api/CustomArticles");
                }

                if (DgArticles != null) DgArticles.ItemsSource = articles;
                if (DgCrud != null) DgCrud.ItemsSource = articles;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri dohvatu podataka: {ex.Message}");
            }
        }

        private void DgMainArticles_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (DgArticles.SelectedItem is ArticleDto selected && DgCrud != null)
            {
                DgCrud.SelectedItem = selected;
            }
        }

        private void DgCrud_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (DgCrud.SelectedItem is ArticleDto selected)
            {
                TxtCrudTitle.Text = selected.Title;
                TxtCrudSlug.Text = selected.Slug;
                TxtCrudContent.Text = selected.Content;
            }
        }

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

                string endpoint = (RbCosmic != null && RbCosmic.IsChecked == true)
                    ? "/api/Articles/cosmic"
                    : "/api/CustomArticles";

                var response = await _httpClient.PostAsJsonAsync(endpoint, newArticle);
                if (response.IsSuccessStatusCode)
                {
                    string target = (RbCosmic != null && RbCosmic.IsChecked == true) ? "Cosmic CMS" : "Bazu";
                    MessageBox.Show($"Članak uspješno dodan na {target}!");
                    await UcitajPodatkeOvisnoOIzvoruAsync();
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

                string endpoint = (RbCosmic != null && RbCosmic.IsChecked == true)
                    ? $"/api/Articles/cosmic/{selected.Id}"
                    : $"/api/CustomArticles/{selected.Id}";

                var response = await _httpClient.PutAsJsonAsync(endpoint, selected);
                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Članak uspješno ažuriran!");
                    await UcitajPodatkeOvisnoOIzvoruAsync();
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

        private async void BtnCrudDelete_Click(object sender, RoutedEventArgs e)
        {
            if (DgCrud.SelectedItem is not ArticleDto selected)
            {
                MessageBox.Show("Molimo odaberite članak iz tablice za brisanje.");
                return;
            }

            try
            {
                string endpoint = (RbCosmic != null && RbCosmic.IsChecked == true)
                    ? $"/api/Articles/cosmic/{selected.Id}"
                    : $"/api/CustomArticles/{selected.Id}";

                var response = await _httpClient.DeleteAsync(endpoint);
                if (response.IsSuccessStatusCode)
                {
                    MessageBox.Show("Članak obrisan!");
                    await UcitajPodatkeOvisnoOIzvoruAsync();
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

        private async void BtnSendJson_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var content = new StringContent(TxtJsonInput.Text, Encoding.UTF8, "application/json");
                var response = await _httpClient.PostAsync("/api/Articles/upload-json", content);
                var responseText = await response.Content.ReadAsStringAsync();

                MessageBox.Show(responseText, response.IsSuccessStatusCode ? "Uspjeh" : "Validacijska Greška");
                if (response.IsSuccessStatusCode) await UcitajPodatkeOvisnoOIzvoruAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

        private async void BtnSendXml_Click(object sender, RoutedEventArgs e)
        {
            try
            {
                var content = new StringContent($"\"{TxtXmlInput.Text.Replace("\"", "\\\"").Replace("\n", "").Replace("\r", "")}\"", Encoding.UTF8, "application/json");
                var response = await _httpClient.PostAsync("/api/Articles/upload-xml", content);
                var responseText = await response.Content.ReadAsStringAsync();

                MessageBox.Show(responseText, response.IsSuccessStatusCode ? "Uspjeh" : "Validacijska Greška");
                if (response.IsSuccessStatusCode) await UcitajPodatkeOvisnoOIzvoruAsync();
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška: {ex.Message}");
            }
        }

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

                int counter = 1;
                foreach (var el in xDoc.Descendants().Where(x => x.Name.LocalName.Equals("ArticleDto", StringComparison.OrdinalIgnoreCase) || x.Name.LocalName.Equals("Article", StringComparison.OrdinalIgnoreCase) || x.Name.LocalName.Equals("ArticleEntity", StringComparison.OrdinalIgnoreCase)))
                {
                    var meta = el.Elements().FirstOrDefault(x => x.Name.LocalName.Equals("Metadata", StringComparison.OrdinalIgnoreCase) || x.Name.LocalName.Equals("metadata", StringComparison.OrdinalIgnoreCase));

                    string GetVal(params string[] names)
                    {
                        foreach (var name in names)
                        {
                            var val = el.Elements().FirstOrDefault(x => x.Name.LocalName.Equals(name, StringComparison.OrdinalIgnoreCase))?.Value;
                            if (!string.IsNullOrEmpty(val)) return val;

                            if (meta != null)
                            {
                                val = meta.Elements().FirstOrDefault(x => x.Name.LocalName.Equals(name, StringComparison.OrdinalIgnoreCase))?.Value;
                                if (!string.IsNullOrEmpty(val)) return val;
                            }
                        }
                        return string.Empty;
                    }

                    string id = GetVal("Id", "id", "_id", "ArticleId", "article_id", "ID");
                    if (string.IsNullOrEmpty(id))
                    {
                        id = counter.ToString(); 
                    }

                    string title = GetVal("Title", "title");
                    string slug = GetVal("Slug", "slug");
                    if (string.IsNullOrEmpty(slug) && !string.IsNullOrEmpty(title))
                    {
                        slug = title.ToLower().Replace(" ", "-");
                    }

                    articles.Add(new ArticleDto
                    {
                        Id = id,
                        Title = title,
                        Slug = slug,
                        Content = GetVal("Content", "content"),
                        Summary = GetVal("Summary", "summary"),
                        Metadata = new ArticleMetadata
                        {
                            Summary = GetVal("Summary", "summary"),
                            Content = GetVal("Content", "content"),
                            Author = GetVal("Author", "author", "creator"),
                            Category = GetVal("Category", "category", "type")
                        }
                    });

                    counter++;
                }

                DgSoapResults.ItemsSource = articles;
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Greška pri SOAP pozivu: {ex.Message}");
            }
        }

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