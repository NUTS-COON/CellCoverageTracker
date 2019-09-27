using Api.Models;
using Api.Settings;
using MongoDB.Driver;

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

        public long CountByImei(string imei)
        {
            return _data.CountDocuments(book => true);
        }

        public void Add(CellInfo item)
        {
            _data.InsertOne(item);
        }
    }
}