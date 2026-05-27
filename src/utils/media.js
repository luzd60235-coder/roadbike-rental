import loginHeroImage from '@/assets/illustrations/login-cycling-scene.svg'
import bikeTarmacImage from '@/assets/bikes-photo/bike-tarmac-sl8-comp.webp'
import bikeMadoneImage from '@/assets/bikes-photo/bike-madone-sl-6.jpg'
import bikeDefyImage from '@/assets/bikes-photo/bike-defy-advanced-1.jpg'
import bikeSupersixImage from '@/assets/bikes-photo/bike-supersix-evo-3.png'
import storeHongqiaoImage from '@/assets/illustrations/store-hongqiao.svg'
import storeLujiazuiImage from '@/assets/illustrations/store-lujiazui.svg'
import storeWestLakeImage from '@/assets/illustrations/store-west-lake.svg'
import storeJinjiLakeImage from '@/assets/illustrations/store-jinji-lake.svg'

const BIKE_IMAGE_BY_MODEL_ID = {
  9401: bikeTarmacImage,
  9402: bikeMadoneImage,
  9403: bikeDefyImage,
  9404: bikeSupersixImage
}
// 车辆图片、门店图片等资源映射工具。
const LOGIN_SHOWCASE_BIKES = [
  {
    id: 'tarmac',
    name: 'Specialized Tarmac SL8 Comp',
    imageUrl: bikeTarmacImage,
    accent: 'rgba(15, 118, 110, 0.18)'
  },
  {
    id: 'madone',
    name: 'TREK Madone SL 6',
    imageUrl: bikeMadoneImage,
    accent: 'rgba(29, 78, 216, 0.18)'
  },
  {
    id: 'defy',
    name: 'GIANT Defy Advanced 1',
    imageUrl: bikeDefyImage,
    accent: 'rgba(245, 158, 11, 0.18)'
  },
  {
    id: 'supersix',
    name: 'Cannondale SuperSix EVO 3',
    imageUrl: bikeSupersixImage,
    accent: 'rgba(248, 113, 113, 0.16)'
  }
]

function normalizeAssetUrl(url = '') {
  if (!url) {
    return ''
  }
  if (/^(https?:)?\/\//.test(url) || url.startsWith('data:') || url.startsWith('blob:')) {
    return url
  }
  return url.startsWith('/') ? url : `/${url.replace(/^\.?\//, '')}`
}

export function resolveBikeImage(bike = {}) {
  const uploadedImage = normalizeAssetUrl(bike?.coverImageUrl)
  if (uploadedImage) {
    return uploadedImage
  }

  const modelId = Number(bike?.modelId)
  if (BIKE_IMAGE_BY_MODEL_ID[modelId]) {
    return BIKE_IMAGE_BY_MODEL_ID[modelId]
  }

  const signature = `${bike?.brandName || bike?.brand || ''} ${bike?.modelName || bike?.model || ''}`.toLowerCase()
  if (signature.includes('specialized') || signature.includes('tarmac')) {
    return bikeTarmacImage
  }
  if (signature.includes('trek') || signature.includes('madone')) {
    return bikeMadoneImage
  }
  if (signature.includes('giant') || signature.includes('defy')) {
    return bikeDefyImage
  }
  return bikeSupersixImage
}

export function resolveStoreImage(store = {}) {
  const uploadedImage = normalizeAssetUrl(store?.storeImageUrl || store?.imageUrl)
  if (uploadedImage) {
    return uploadedImage
  }

  const name = `${store?.storeName || store?.name || ''}`.toLowerCase()
  const city = `${store?.city || ''}`.toLowerCase()
  const district = `${store?.district || ''}`.toLowerCase()

  if (name.includes('虹桥') || district.includes('闵行')) {
    return storeHongqiaoImage
  }
  if (name.includes('陆家嘴') || district.includes('浦东')) {
    return storeLujiazuiImage
  }
  if (name.includes('西湖') || city.includes('杭州')) {
    return storeWestLakeImage
  }
  if (name.includes('金鸡湖') || city.includes('苏州')) {
    return storeJinjiLakeImage
  }
  return storeHongqiaoImage
}

export { loginHeroImage, LOGIN_SHOWCASE_BIKES }
