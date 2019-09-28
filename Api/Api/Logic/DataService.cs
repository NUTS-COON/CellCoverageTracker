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
        private readonly IMongoCollection<CellInfo> _data;

        public DataService(MongoSettings settings)
        {
            var client = new MongoClient(settings.ConnectionString);
            var database = client.GetDatabase(settings.DatabaseName);

            _data = database.GetCollection<CellInfo>(settings.CollectionName);
        }

        public async Task<long> CountByImei(string imei) => await _data.CountDocumentsAsync(x => x.IMEI == imei);

        public async Task Add(CellInfo item) => await _data.InsertOneAsync(item);

        public async Task Add(CellInfo[] items) => await _data.InsertManyAsync(items);

        public async Task<IEnumerable<CellPoint>> Get(RectangleOfSearch model)
        {
            var resultFilter = Builders<CellInfo>.Filter.Empty;

            if (model.Filter?.Level != null)
                resultFilter &= Builders<CellInfo>.Filter.Eq(c => c.Level, model.Filter.Level);

            if (!string.IsNullOrEmpty(model.Filter?.CellType))
                resultFilter &= Builders<CellInfo>.Filter.Eq(c => c.CellType, model.Filter.CellType);

            if (!string.IsNullOrEmpty(model.Filter?.OperatorName))
                resultFilter &= Builders<CellInfo>.Filter.Eq(c => c.OperatorName, model.Filter.OperatorName);

            if (model.LeftBottomCorner != null && model.RightTopCorner != null)
                resultFilter &= GetBetweenFilter(model.LeftBottomCorner, model.RightTopCorner);

            return (await _data.FindAsync(resultFilter))?.ToList()?.Select(c => new CellPoint
            {
                CellType = c.CellType,
                Latitude = c.Latitude,
                Longitude = c.Longitude,
                Level = c.Level,
                OperatorName = c.OperatorName
            });
        }

        private FilterDefinition<CellInfo> GetBetweenFilter(Coordinate leftBottomCorner, Coordinate rightTopCorner)
        {
            return new FilterDefinitionBuilder<CellInfo>().And(
                Builders<CellInfo>.Filter.Gte(c => c.Latitude, leftBottomCorner.Latitude),
                Builders<CellInfo>.Filter.Gte(c => c.Longitude, leftBottomCorner.Longitude),
                Builders<CellInfo>.Filter.Lte(c => c.Latitude, rightTopCorner.Latitude),
                Builders<CellInfo>.Filter.Lte(c => c.Longitude, rightTopCorner.Longitude));
        }
    }
}