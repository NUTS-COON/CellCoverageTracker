using Api.Models;
using Api.Settings;
using MongoDB.Driver;
using System.Threading.Tasks;

namespace Api.Logic
{
    public class DataService
    {
        private readonly IMongoCollection<CellInfo> _data;

        public DataService(MongoSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _data = database.GetCollection<CellInfo>(settings.CollectionName);
        }

        public async Task<long> CountByImei(string imei)
        {
            return await _data.CountDocumentsAsync(x => x.IMEI == imei);
        }

        public async Task Add(CellInfo item)
        {
            await _data.InsertOneAsync(item);
        }

        public async Task Add(CellInfo[] items)
        {
            await _data.InsertManyAsync(items);
        }
    }
}