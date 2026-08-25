using System.ServiceModel;
using Interop.Common;

namespace Interop.Server.Services
{
    [ServiceContract]
    public interface ISoapService
    {
        [OperationContract]
        Task<List<ArticleDto>> SearchArticlesXml(string searchTerm);
    }
}