using Api.Models;
using Api.Settings;
using MongoDB.Driver;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace Api.Logic
{
    public class DataService
    {
        private readonly IMongoCollection<CellInfoMongoModel> _data;

        public DataService(MongoSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _data = database.GetCollection<CellInfoMongoModel>(settings.CollectionName);
        }

        public async Task<long> CountByImei(string imei) => await _data.CountDocumentsAsync(x => x.IMEI == imei);

        public async Task Add(CellInfoSaveRequest item)
        {
            var data = new CellInfoMongoModel(item);
            await _data.InsertOneAsync(data);
        }

        public async Task Add(CellInfoSaveRequest[] items) => await _data.InsertManyAsync(items.Select(i => new CellInfoMongoModel(i)));

        public async Task<IEnumerable<CellPoint>> SearchGeo(Coordinate[] points)
        {
            var minX = points.Select(t => t.Longitude).Min();
            var maxX = points.Select(t => t.Longitude).Max();
            var minY = points.Select(t => t.Latitude).Min();
            var maxY = points.Select(t => t.Latitude).Max();
            var filter = Builders<CellInfoMongoModel>.Filter.GeoWithinBox(c => c.Location, minX, maxX, minY,maxY);

            return (await _data.FindAsync(filter))?.ToList()?.Select(c => new CellPoint(c));
        }
    }
}