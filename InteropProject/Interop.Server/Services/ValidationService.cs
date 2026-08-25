using System.Xml;
using System.Xml.Schema;
using NJsonSchema;
using Interop.Server.Data;

namespace Interop.Server.Services
{
    public class ValidationService
    {
        private readonly string _xsdPath;
        private readonly string _jsonSchemaPath;

        public ValidationService(IWebHostEnvironment env)
        {
            _xsdPath = System.IO.Path.Combine(env.ContentRootPath, "Schemas", "article.xsd");
            _jsonSchemaPath = System.IO.Path.Combine(env.ContentRootPath, "Schemas", "article.json");
        }

        public List<string> ValidateXml(string xmlContent)
        {
            var errors = new List<string>();

            try
            {
                XmlSchemaSet schemas = new XmlSchemaSet();
                schemas.Add("", _xsdPath);

                XmlReaderSettings settings = new XmlReaderSettings
                {
                    ValidationType = ValidationType.Schema,
                    Schemas = schemas
                };

                settings.ValidationEventHandler += (sender, args) =>
                {
                    errors.Add($"[XML Line {args.Exception?.LineNumber}]: {args.Message}");
                };

                using XmlReader reader = XmlReader.Create(new StringReader(xmlContent), settings);
                while (reader.Read()) { }
            }
            catch (Exception ex)
            {
                errors.Add($"Greška parsiranja XML-a: {ex.Message}");
            }

            return errors;
        }

        public async Task<List<string>> ValidateJsonAsync(string jsonContent)
        {
            var errors = new List<string>();

            try
            {
                var schema = await JsonSchema.FromFileAsync(_jsonSchemaPath);
                var jsonErrors = schema.Validate(jsonContent);

                foreach (var error in jsonErrors)
                {
                    errors.Add($"[JSON Svojstvo '{error.Path}']: {error.Kind}");
                }
            }
            catch (Exception ex)
            {
                errors.Add($"Greška parsiranja JSON-a: {ex.Message}");
            }

            return errors;
        }
    }
}